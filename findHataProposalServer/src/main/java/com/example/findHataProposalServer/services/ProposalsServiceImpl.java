package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.algorithms.KMP;
import com.example.findHataProposalServer.entities.*;
import com.example.findHataProposalServer.entities.requests.ChangeProposalRequest;
import com.example.findHataProposalServer.entities.responses.ProposalResponse;
import com.example.findHataProposalServer.exceptions.NoFoundProposalException;
import com.example.findHataProposalServer.exceptions.NoRightException;
import com.example.findHataProposalServer.repositories.ImagePathRepository;
import com.example.findHataProposalServer.repositories.ProposalRepository;
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

    @Value("${externalUrl}")
    String externalUrl;



    @PostConstruct
    void init() {
        ApplicationHome home = new ApplicationHome(ProposalsServiceImpl.class);

        ObjectMapper mapper = new ObjectMapper();

        System.out.println("init path: " + home.getDir().getAbsoluteFile() + "/init.json");

        File file = new File(home.getDir().getAbsoluteFile() + "/init.json");

        InitData init;
        try {
            init = mapper.readValue(file, InitData.class);

            for (InitData.InitProposal proposal: init.getProposals()) {

                List<ImagePath> paths = new ArrayList<>();
                for (String image: proposal.getImages()) {
                    ImagePath imagePath = ImagePath.builder()
                            .path(externalUrl + "/" + serviceName + image)
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
            }
        } catch (IOException e) {
            e.printStackTrace();
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

            proposalRepository.save(ProposalBD.builder()
                    .title(proposal.getTitle())
                    .description(proposal.getDescription())
                    .location(proposal.getLocation())
                    .ownerId(proposal.getOwnerId())
                    .price(proposal.getPrice())
                    .images(images)
                    .build()
            );


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

    private List<ShortInfoProposal> convertProposal(List<ProposalBD> list) {
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

        return convertProposal(proposals);

    }

    @Override
    public List<ShortInfoProposal> getAllWithKeyword(String keyword) {
        List<ProposalBD> proposals = proposalRepository.findAll(
                (Specification<ProposalBD>) (root, query, criteriaBuilder) -> {

            Predicate predicate1 = criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
            Predicate predicate2 = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");
            Predicate predicate3 = criteriaBuilder.like(root.get("location"), "%" + keyword + "%");

            return criteriaBuilder.or(predicate1, predicate2, predicate3);
        });

        proposals.sort((o1, o2) -> {
            String fullText = o1.getTitle() + o1.getDescription() + o1.getLocation();
            String fullText2 = o2.getTitle() + o2.getDescription() + o2.getLocation();
            return Integer.compare(
                    KMP.solve(fullText.toLowerCase(), keyword.toLowerCase()).size(),
                    KMP.solve(fullText2.toLowerCase(), keyword.toLowerCase()).size() * -1
            );
        });

        return convertProposal(proposals);
    }

    @Override
    public void removeProposal(int proposalId, int userId) throws NoFoundProposalException, NoRightException {
        Optional<ProposalBD> proposalBD = proposalRepository.findById(proposalId);

        if (proposalBD.isPresent()) {
            if (proposalBD.get().getOwnerId() != userId) {
                throw new NoRightException("The user does not have the right to delete the proposal");
            }

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
                proposalRepository.delete(proposalBD.get());
            } else {
                throw new NoFoundProposalException("proposal not found");
            }
        } else {
            throw new NoRightException("The user does not have the right to delete the proposal");
        }
    }

    @Override
    public void changeProposal(int proposalId, int userId, ChangeProposalRequest changes) throws NoFoundProposalException, NoRightException {
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
    public void changeProposal(List<Role> roles, int proposalId, ChangeProposalRequest changes) throws NoFoundProposalException, NoRightException {
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
