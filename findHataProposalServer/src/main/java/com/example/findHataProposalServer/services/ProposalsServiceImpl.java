package com.example.findHataProposalServer.services;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.*;
import akka.stream.javadsl.*;
import com.example.findHataProposalServer.Profile;
import com.example.findHataProposalServer.algorithms.KMP;
import com.example.findHataProposalServer.algorithms.kdb.ByteTransform;
import com.example.findHataProposalServer.algorithms.kdb.KDBTree;
import com.example.findHataProposalServer.algorithms.kdb.KDBTreeDBDriver;
import com.example.findHataProposalServer.algorithms.kdb.VectorRep;
import com.example.findHataProposalServer.entities.*;
import com.example.findHataProposalServer.entities.requests.ChangeProposalRequest;
import com.example.findHataProposalServer.entities.responses.ProposalResponse;
import com.example.findHataProposalServer.exceptions.NoFoundProposalException;
import com.example.findHataProposalServer.exceptions.NoRightException;
import com.example.findHataProposalServer.repositories.ImagePathRepository;
import com.example.findHataProposalServer.repositories.ProposalRepository;
import com.example.findHataProposalServer.repositories.ReverseProposalFactIndexRep;
import com.example.findHataProposalServer.repositories.VectorizedFactRep;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;


@Service
public class ProposalsServiceImpl implements ProposalsService {

    @Autowired
    ProposalRepository proposalRepository;

    @Autowired
    ImagePathRepository imagePathRepository;

    @Value("${spring.application.name}")
    String serviceName;

    KDBTree kdbTree;

    @Autowired
    KDBTreeDBDriver kdbTreeDBDriver;

    @Autowired
    VectorRep vectorRep;

    @Autowired
    VectorizationServiceRep vecClient;

    @Autowired
    ReverseProposalFactIndexRep reverseProposalFactIndexRep;

    @Autowired
    VectorizedFactRep vectorizedFactRep;

    @Autowired
    ActorSystem actorSystem;

    ActorRef actorOfIndexingProposals;

    final AtomicInteger counter = new AtomicInteger();

    private ClosedShape buildGraph(GraphDSL.Builder<Pair<ActorRef, CompletionStage<Done>>> builder,
                                   SourceShape<ProposalDB> src,
                                   SinkShape<Pair<ProposalDB, Profile.VectorizedProposal>> snk) {
        var unzipFlow = UnzipWith
                .create((ProposalDB v) -> {
                    Profile.Proposal proposal = Profile.Proposal.newBuilder()
                            .setTitle(v.getTitle())
                            .setDescription(v.getDescription())
                            .setLocation(v.getLocation())
                            .build();
                    return Pair.create(proposal, v);
                });

        var unzip = builder.add(unzipFlow);
        var merge = builder.add(
                ZipWith.create((Profile.VectorizedProposal f, ProposalDB s) -> Pair.create(s, f))
        );

//        var buffer = Flow.of(Profile.VectorizedProposal.class).buffer(100, OverflowStrategy.backpressure());

        Sink<Profile.Proposal, Publisher<Profile.Proposal>> proposalPub =
                Sink.asPublisher(AsPublisher.WITHOUT_FANOUT);
        var mat = proposalPub.preMaterialize(actorSystem);
        var proposalSub = Source.fromPublisher(mat.first());
        var responseStream = vecClient.client.vectorizeProposals(proposalSub);

        builder.from(unzip.out0())
                .to(builder.add(mat.second().async()));

        builder.from(builder.add(responseStream.async()))
                .toInlet(merge.in0());

        builder.from(unzip.out1())
                .toInlet(merge.in1());

        builder.from(src).toInlet(unzip.in());

        builder.from(merge.out())
                .to(snk);


        return ClosedShape.getInstance();

    }

    void initAkkaStream() {
        Source<ProposalDB, ActorRef> source = Source.actorRef(elem -> {
                    if (elem == Done.done()) return Optional.of(CompletionStrategy.immediately());
                    else return Optional.empty();
                },
                elem -> Optional.empty(),
                100,
                OverflowStrategy.fail());

//        AtomicLong all = new AtomicLong();
        Sink<Pair<ProposalDB, Profile.VectorizedProposal>, CompletionStage<Done>> sink = Sink
                .foreachAsync(10, (v) -> CompletableFuture.runAsync(() -> {
//                    long start = System.currentTimeMillis();
                    try {
//                        long prev = all.get();

                        for (var vec : v.second().getVectorList()) {
                            double[] arr = vec.getVectorList().stream().mapToDouble(Double::doubleValue).toArray();
                            long kdbVecId = kdbTree.insert(arr);
                            VectorizedFact vectorizedFact = vectorizedFactRep.findById(kdbVecId).orElse(null);

                            ReverseProposalFactIndex index = ReverseProposalFactIndex.builder()
                                    .proposalDB(v.first())
                                    .vectorizedFact(vectorizedFact)
                                    .build();

                            reverseProposalFactIndexRep.save(index);

                        }
//                        long change = System.currentTimeMillis() - start;
//                        all.addAndGet(change);
//                        System.out.println("время на запись в дерево: " + all + " мс"
//                                + " ( + " + (all.get() - prev) + " мс )");

                        counter.incrementAndGet();
                        synchronized (counter) {
                            counter.notify();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

        Graph<ClosedShape, Pair<ActorRef, CompletionStage<Done>>> graph = GraphDSL.create(
                source, sink, Pair::new, this::buildGraph);

        RunnableGraph<Pair<ActorRef, CompletionStage<Done>>> runnableGraph =
                RunnableGraph.fromGraph(graph);

        actorOfIndexingProposals = runnableGraph.run(actorSystem).first();
    }


    @PostConstruct
    void init() {
        long startTime = System.currentTimeMillis();
        initAkkaStream();
        int k = 50;
        if (proposalRepository.findAll().size() != 0) {
            kdbTree = new KDBTree(300, k, kdbTreeDBDriver, vectorRep, kdbTreeDBDriver.getRoot());
            return;
        }
        kdbTree = new KDBTree(300, k, kdbTreeDBDriver, vectorRep, null);

        ApplicationHome home = new ApplicationHome(ProposalsServiceImpl.class);

        ObjectMapper mapper = new ObjectMapper();

        String initPath = home.getDir().getAbsoluteFile() + "/init.json";
//        String initPath = "./docker/init.json";
//        String initPath = "./docker/prev_init.json";
        System.out.println("init path: " + initPath);

        File file = new File(initPath);

        InitData init;
        try {
            init = mapper.readValue(file, InitData.class);

            for (InitData.InitProposal proposal : init.getProposals()) {

                List<ImagePath> paths = new ArrayList<>();
                for (String image : proposal.getImages()) {
                    ImagePath imagePath = ImagePath.builder()
                            .path("/" + serviceName + image)
                            .build();

                    imagePathRepository.save(imagePath);
                    paths.add(imagePath);
                }
                ProposalDB proposalDB = ProposalDB.builder()
                        .location(proposal.getLocation())
                        .images(paths)
                        .title(proposal.getTitle())
                        .description(proposal.getDescription())
                        .ownerId(proposal.getOwnerId())
                        .price(proposal.getPrice())
                        .build();

                proposalRepository.save(proposalDB);
                try {
//                    CompletionStage<Object> future = ask(saveProposalActor, proposalDB, Duration.ofSeconds(1));
                    actorOfIndexingProposals.tell(proposalDB, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            synchronized (counter) {
                counter.wait(100000);
            }
            int prev = 0;
            while (true) {
                if (counter.get() == init.getProposals().size()) {
                    break;
                } else {
                    if (counter.get() == prev) {
                        System.out.println("the proposal has not been saved");
                        break;
                    }
                    synchronized (counter) {
                        prev = counter.get();
                        counter.wait(100000);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        long stopTime = System.currentTimeMillis();
        System.out.println((stopTime - startTime) / 1000 + " s");
    }

    @Override
    @Transactional
    public void addProposal(List<Role> roles, Proposal proposal) throws NoRightException {

        if (roles.contains(Role.USER) || roles.contains(Role.ADMIN) || roles.contains(Role.OTHER_SERVICE)) {

            List<ImagePath> images = new ArrayList<>();
            for (String path : proposal.getImages()) {
                ImagePath imagePath = ImagePath.builder()
                        .path(path)
                        .build();

                ImagePath res = imagePathRepository.save(imagePath);
                images.add(res);
            }

            ProposalDB proposalDB = proposalRepository.save(ProposalDB.builder()
                    .title(proposal.getTitle())
                    .description(proposal.getDescription())
                    .location(proposal.getLocation())
                    .ownerId(proposal.getOwnerId())
                    .price(proposal.getPrice())
                    .images(images)
                    .build()
            );
            actorOfIndexingProposals.tell(proposalDB, null);
        } else {
            throw new NoRightException("The user does not have the right to add an proposal");
        }
    }

    @Override
    public ProposalResponse getInfoAboutProposal(Integer proposalId) throws NoFoundProposalException {
        Optional<ProposalDB> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            return ProposalResponse.fromBD(proposalBD.get());
        }

        throw new NoFoundProposalException("proposal not found");
    }

    private List<ShortInfoProposal> convertToShortProposal(List<ProposalDB> list) {
        return list.stream().map((v) -> ShortInfoProposal.builder()
                .id(v.getId())
                .location(v.getLocation())
                .price(v.getPrice())
                .title(v.getTitle())
                .images(v.getImages().stream().map(ImagePath::getPath).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<ShortInfoProposal> getAll() {
        List<ProposalDB> proposals = proposalRepository.findAll();
        return convertToShortProposal(proposals);
    }

    static double[] delta = new double[300];

    static {
        Arrays.fill(delta, 0.5);
    }

    double cosSimilarity(double[] f, double[] s) {
        double mult = 0;
        double f_normal = 0;
        double s_normal = 0;
        for (int i = 0; i < f.length; i++) {
            mult += f[i] * s[i];
            f_normal += Math.pow(f[i], 2);
            s_normal += Math.pow(s[i], 2);
        }
        f_normal = Math.sqrt(f_normal);
        s_normal = Math.sqrt(s_normal);
        return (Math.abs(mult) / f_normal) / s_normal;
    }

    public List<ShortInfoProposal> getAllWithKeywordKDBTreeImpl(String keyword) {
        Profile.Request request = Profile.Request.newBuilder().setRequest(keyword).build();

        var res = vecClient.client.vectorizeRequest(request);

        var sink = StreamConverters.<Profile.TextVector>asJavaStream();
        var it = res.runWith(sink, actorSystem);

        Map<Integer, Double> ansIds = new HashMap<>();

        it.forEach((vec) -> {
            long time = System.currentTimeMillis();
            double[] vectorizedKeyword = vec.getVectorList().stream().mapToDouble(Double::doubleValue).toArray();
            List<Long> ids = kdbTree.find(vectorizedKeyword, delta);
            long newTime = System.currentTimeMillis();
            System.out.println(newTime - time);

            for (Long id : ids) {
                var vecFact = vectorizedFactRep.findById(id).orElse(null);
                for (ReverseProposalFactIndex index : reverseProposalFactIndexRep.findAllByVectorizedFact(vecFact)) {
                    ansIds.merge(index.getProposalDB().getId(),
                            cosSimilarity(vectorizedKeyword, ByteTransform.toDoubleArray(vecFact.getVector())),
                            Math::max);
                }
            }

        });
        List<ProposalDB> proposal = proposalRepository.findAllById(ansIds.keySet());
        proposal.sort(Comparator.comparingDouble((ProposalDB v) -> ansIds.get(v.getId())).reversed());
        return convertToShortProposal(proposal);

    }


    @Override
    public List<ShortInfoProposal> getAllWithKeyword(String keyword) {
        return getAllWithKeywordKDBTreeImpl(keyword);
    }

    public List<ShortInfoProposal> getAllWithKeywordKMP(String keyword) {
        String keywordFinal = keyword.toLowerCase();
        List<ProposalDB> proposals = proposalRepository.findAll(
                (Specification<ProposalDB>) (root, query, criteriaBuilder) -> {

                    Predicate predicate1 = criteriaBuilder.like(criteriaBuilder.lower(
                            root.get("title")), "%" + keywordFinal + "%");
                    Predicate predicate2 = criteriaBuilder.like(criteriaBuilder.lower(
                            root.get("description")), "%" + keywordFinal + "%");
                    Predicate predicate3 = criteriaBuilder.like(criteriaBuilder.lower(
                            root.get("location")), "%" + keywordFinal + "%");

                    return criteriaBuilder.or(predicate1, predicate2, predicate3);
                });

        proposals.sort((o1, o2) -> {
            String fullText = o1.getTitle() + o1.getDescription() + o1.getLocation();
            String fullText2 = o2.getTitle() + o2.getDescription() + o2.getLocation();
            return Integer.compare(
                    KMP.solve(fullText.toLowerCase(), keywordFinal).size(),
                    KMP.solve(fullText2.toLowerCase(), keywordFinal).size()
            ) * -1;
        });

        return convertToShortProposal(proposals);
    }

    public void removeProposalImpl(ProposalDB proposal) {
        List<ReverseProposalFactIndex> facts = reverseProposalFactIndexRep.findAllByProposalDB(proposal);
        for (ReverseProposalFactIndex reverseFact : facts) {
            List<ReverseProposalFactIndex> f = reverseProposalFactIndexRep
                    .findAllByVectorizedFact(reverseFact.getVectorizedFact());

            if (f.size() > 1) {
                reverseProposalFactIndexRep.delete(reverseFact);
            } else if (f.size() == 1) {
                reverseProposalFactIndexRep.delete(reverseFact);
                kdbTree.remove(reverseFact.getVectorizedFact().getId());
            }
        }
        proposalRepository.delete(proposal);
    }

    @Override
    @Transactional
    public void removeProposal(int proposalId, int userId) throws NoFoundProposalException, NoRightException {
        Optional<ProposalDB> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            if (proposalBD.get().getOwnerId() != userId) {
                throw new NoRightException("The user does not have the right to delete the proposal");
            }
            removeProposalImpl(proposalBD.get());
        } else {
            throw new NoFoundProposalException("proposal not found");
        }

    }

    @Override
    @Transactional
    public void removeProposal(List<Role> roles, int proposalId) throws NoRightException, NoFoundProposalException {
        if (roles.contains(Role.ADMIN) || roles.contains(Role.OTHER_SERVICE)) {
            Optional<ProposalDB> proposalBD = proposalRepository.findById(proposalId);

            if (proposalBD.isPresent()) {
                removeProposalImpl(proposalBD.get());
            } else {
                throw new NoFoundProposalException("proposal not found");
            }
        } else {
            throw new NoRightException("The user does not have the right to delete the proposal");
        }
    }

    @Override
    public void changeProposal(int proposalId, int userId, ChangeProposalRequest changes)
            throws NoFoundProposalException, NoRightException {
        Optional<ProposalDB> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            if (proposalBD.get().getOwnerId() != userId) {
                throw new NoRightException("The user does not have the right to change the proposal");
            }

            ProposalDB proposal = proposalBD.get();

            changes.apply(proposal);
            proposalRepository.save(proposal);
        }

        throw new NoFoundProposalException("proposal not found");
    }

    @Override
    public void changeProposal(List<Role> roles, int proposalId, ChangeProposalRequest changes)
            throws NoFoundProposalException, NoRightException {
        if (roles.contains(Role.ADMIN) || roles.contains(Role.OTHER_SERVICE)) {
            Optional<ProposalDB> proposalBD = proposalRepository.findById(proposalId);

            if (proposalBD.isPresent()) {

                ProposalDB proposal = proposalBD.get();

                changes.apply(proposal);
                proposalRepository.save(proposal);

            } else {
                throw new NoFoundProposalException("proposal not found");
            }
        } else {
            throw new NoRightException("The user does not have the right to change the proposal");
        }
    }
}
