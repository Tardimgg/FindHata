package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.Profile;
import com.example.findHataProposalServer.algorithms.KMP;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    VectorizationServiceClient vecClient;

    @Autowired
    ReverseProposalFactIndexRep reverseProposalFactIndexRep;

    @Autowired
    VectorizedFactRep vectorizedFactRep;

    @PostConstruct
    void init() {
        if (proposalRepository.findAll().size() != 0) {
            return;
        }
        kdbTree = new KDBTree(300, 50, kdbTreeDBDriver, vectorRep, null);

        ApplicationHome home = new ApplicationHome(ProposalsServiceImpl.class);

        ObjectMapper mapper = new ObjectMapper();

        String initPath = home.getDir().getAbsoluteFile() + "/init.json";
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
                ProposalBD proposalBD = ProposalBD.builder()
                        .location(proposal.getLocation())
                        .images(paths)
                        .title(proposal.getTitle())
                        .description(proposal.getDescription())
                        .ownerId(proposal.getOwnerId())
                        .price(proposal.getPrice())
                        .build();

                proposalRepository.save(proposalBD);
                try {
                    saveVectorInfo(proposalBD);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveVectorInfo(ProposalBD proposal) {
        Profile.Proposal proposalRequest = Profile.Proposal.newBuilder()
                .setTitle(proposal.getTitle())
                .setDescription(proposal.getDescription())
                .setLocation(proposal.getLocation())
                .build();

        var ansIter = vecClient.blockingStub.vectorizeProposal(proposalRequest);
        while (ansIter.hasNext()) {
            Profile.TextVector vec = ansIter.next();

            double[] doubleVec = vec.getVectorList().stream().mapToDouble(Double::doubleValue).toArray();

            long kdbVecId = kdbTree.insert(doubleVec);

            VectorizedFact vectorizedFact = vectorizedFactRep.findById(kdbVecId).orElse(null);

            ReverseProposalFactIndex index = ReverseProposalFactIndex.builder()
                    .proposalBD(proposal)
                    .vectorizedFact(vectorizedFact)
                    .build();

            reverseProposalFactIndexRep.save(index);
        }
    }

    @Override
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

            ProposalBD proposalBD = proposalRepository.save(ProposalBD.builder()
                    .title(proposal.getTitle())
                    .description(proposal.getDescription())
                    .location(proposal.getLocation())
                    .ownerId(proposal.getOwnerId())
                    .price(proposal.getPrice())
                    .images(images)
                    .build()
            );
            saveVectorInfo(proposalBD);


        } else {
            throw new NoRightException("The user does not have the right to add an proposal");
        }
    }

    @Override
    public ProposalResponse getInfoAboutProposal(Integer proposalId) throws NoFoundProposalException {
        Optional<ProposalBD> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            return ProposalResponse.fromBD(proposalBD.get());
        }

        throw new NoFoundProposalException("proposal not found");
    }

    private List<ShortInfoProposal> convertToShortProposal(List<ProposalBD> list) {
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
        List<ProposalBD> proposals = proposalRepository.findAll();

        return convertToShortProposal(proposals);

    }



    static double[] delta = new double[300];
    static {
        Arrays.fill(delta, 0.5);
    }

    public List<ShortInfoProposal> getAllWithKeywordKDBTreeImpl(String keyword) {
                Profile.Request request = Profile.Request.newBuilder().setRequest(keyword).build();
        var ansIter = vecClient.blockingStub.vectorizeRequest(request);


        Set<Integer> ansIds = new HashSet<>();
        while (ansIter.hasNext()) {
            Profile.TextVector vec = ansIter.next();

            long time = System.currentTimeMillis();
            List<Long> ids = kdbTree.find(vec.getVectorList().stream().mapToDouble(Double::doubleValue).toArray(),
                    delta);
            long newTime = System.currentTimeMillis();
            System.out.println(newTime - time);

            for (Long id: ids) {
                var vecFact = vectorizedFactRep.findById(id).orElse(null);
                for (ReverseProposalFactIndex index : reverseProposalFactIndexRep.findAllByVectorizedFact(vecFact)) {
                    ansIds.add(index.getProposalBD().getId());
                }
            }

        }
        return convertToShortProposal(proposalRepository.findAllById(ansIds));
    }


    @Override
    public List<ShortInfoProposal> getAllWithKeyword(String keyword) {
        return getAllWithKeywordKDBTreeImpl(keyword);
    }

    public List<ShortInfoProposal> getAllWithKeywordKMP(String keyword) {
        String keywordFinal = keyword.toLowerCase();
        List<ProposalBD> proposals = proposalRepository.findAll(
                (Specification<ProposalBD>) (root, query, criteriaBuilder) -> {

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

    @Override
    public void removeProposal(int proposalId, int userId) throws NoFoundProposalException, NoRightException {
        Optional<ProposalBD> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            if (proposalBD.get().getOwnerId() != userId) {
                throw new NoRightException("The user does not have the right to delete the proposal");
            }

            System.out.println("!!! the 'delete proposal' function is not implemented when using the kdb tree");
            proposalRepository.delete(proposalBD.get());
        } else {
            throw new NoFoundProposalException("proposal not found");
        }

    }

    @Override
    public void removeProposal(List<Role> roles, int proposalId) throws NoRightException, NoFoundProposalException {
        if (roles.contains(Role.ADMIN) || roles.contains(Role.OTHER_SERVICE)) {
            Optional<ProposalBD> proposalBD = proposalRepository.findById(proposalId);

            if (proposalBD.isPresent()) {
                System.out.println("!!! the 'delete proposal' function is not implemented when using the kdb tree");
                proposalRepository.delete(proposalBD.get());
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
        Optional<ProposalBD> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            if (proposalBD.get().getOwnerId() != userId) {
                throw new NoRightException("The user does not have the right to change the proposal");
            }

            ProposalBD proposal = proposalBD.get();

            changes.apply(proposal);
            proposalRepository.save(proposal);
        }

        throw new NoFoundProposalException("proposal not found");
    }

    @Override
    public void changeProposal(List<Role> roles, int proposalId, ChangeProposalRequest changes)
            throws NoFoundProposalException, NoRightException {
        if (roles.contains(Role.ADMIN) || roles.contains(Role.OTHER_SERVICE)) {
            Optional<ProposalBD> proposalBD = proposalRepository.findById(proposalId);

            if (proposalBD.isPresent()) {

                ProposalBD proposal = proposalBD.get();

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
