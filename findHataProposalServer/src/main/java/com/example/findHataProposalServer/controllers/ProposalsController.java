package com.example.findHataProposalServer.controllers;

import com.example.findHataProposalServer.entities.Proposal;
import com.example.findHataProposalServer.entities.Role;
import com.example.findHataProposalServer.entities.ShortInfoProposal;
import com.example.findHataProposalServer.entities.requests.AddProposalRequest;
import com.example.findHataProposalServer.entities.requests.ChangeProposalRequest;
import com.example.findHataProposalServer.entities.responses.ProposalResponse;
import com.example.findHataProposalServer.exceptions.NoFoundProposalException;
import com.example.findHataProposalServer.exceptions.NoRightException;
import com.example.findHataProposalServer.services.ImageService;
import com.example.findHataProposalServer.services.ProposalsService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proposal")
public class ProposalsController {

    @Autowired
    ProposalsService proposalsService;

    @Autowired
    ImageService imageService;

    @PostMapping
    public Map<String, String> addProposal(@RequestHeader String roles,
                                           @RequestHeader Integer userId,
                                           @RequestHeader String hasAlternativeConnection,
                                           @Validated @NotEmpty @RequestBody AddProposalRequest request) {

        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                        .split(", "))
                .map(Role::valueOf)
                .toList();

        if (userId >= 0 && hasAlternativeConnection.equals("false")) {
            return Map.of("status", "error", "error", "account not confirmed");
        }


        try {
            proposalsService.addProposal(listRoles, Proposal.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .location(request.getLocation())
                    .price(request.getPrice())
                    .ownerId(userId)
                    .images(request.getImages())
                    .build());

            return Map.of("status", "ok");

        } catch (NoRightException e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @PostMapping("/save-image")
    public Map<String, String> addProposal(@RequestHeader String roles,
                                           @RequestHeader Integer userId,
                                           @RequestHeader String hasAlternativeConnection,
                                           @RequestParam("image") MultipartFile image) {

        if (hasAlternativeConnection.equals("false")) {
            return Map.of("status", "error", "error", "account not confirmed");
        }


        String path = imageService.saveImage("1", image);

        return Map.of("status", "ok", "path", path);

    }

    @GetMapping("/get/{id}")
    public Map<String, Object> getProposal(@RequestHeader String roles,
                                           @RequestHeader int userId,
                                           @PathVariable(value="id") int proposalId) {

        try {
            ProposalResponse proposal = proposalsService.getInfoAboutProposal(proposalId);

            return Map.of("status", "ok", "proposal", proposal);

        } catch (NoFoundProposalException e) {
            return Map.of("status", "error", "error", e.getMessage());
        }

    }

    @GetMapping("/get-all")
    public Map<String, Object> getAll(@RequestHeader String roles,
                                      @RequestHeader int userId,
                                      @RequestParam(required = false, name = "keyword") String keyword) {

        List<ShortInfoProposal> proposals;
        if (keyword == null) {
            proposals = proposalsService.getAll();
        } else {
            proposals = proposalsService.getAllWithKeyword(keyword);
        }

        return Map.of("status", "ok", "proposals", proposals);

    }

    @DeleteMapping("/get/{id}")
    public Map<String, String> removeProposal(@RequestHeader String roles,
                                              @RequestHeader int userId,
                                              @PathVariable(value="id") int proposalId) {

        try {
            proposalsService.removeProposal(proposalId, userId);
            return Map.of("status", "ok");

        } catch (NoFoundProposalException e) {
            return Map.of("status", "error", "error", e.getMessage());

        } catch (NoRightException e) {

            List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                            .split(", "))
                    .map(Role::valueOf)
                    .toList();

            try {
                proposalsService.removeProposal(listRoles, proposalId);
                return Map.of("status", "ok");

            } catch (NoRightException ex) {
                return Map.of("status", "error", "error", ex.getMessage());

            } catch (NoFoundProposalException ex) {
                return Map.of("status", "error", "error", "internal error");
            }
        }
    }

    @PostMapping("/get/{id}")
    public Map<String, String> changeProposal(@RequestHeader String roles,
                                              @RequestHeader int userId,
                                              @PathVariable(value="id") int proposalId,
                                              @Validated @NotEmpty @RequestBody ChangeProposalRequest request) {


        try {
            proposalsService.changeProposal(proposalId, userId, request);
            return Map.of("status", "ok");

        } catch (NoFoundProposalException e) {
            return Map.of("status", "error", "error", e.getMessage());


        } catch (NoRightException e) {
            List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                            .split(", "))
                    .map(Role::valueOf)
                    .toList();

            try {
                proposalsService.changeProposal(listRoles, proposalId, request);

                return Map.of("status", "ok");

            } catch (NoFoundProposalException ex) {
                return Map.of("status", "error", "error", "internal error");

            } catch (NoRightException ex) {
                return Map.of("status", "error", "error", ex.getMessage());
            }
        }
    }
}
