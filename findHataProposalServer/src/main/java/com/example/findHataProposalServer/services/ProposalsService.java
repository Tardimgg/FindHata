package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.entities.Proposal;
import com.example.findHataProposalServer.entities.ProposalBD;
import com.example.findHataProposalServer.entities.Role;
import com.example.findHataProposalServer.entities.ShortInfoProposal;
import com.example.findHataProposalServer.entities.requests.ChangeProposalRequest;
import com.example.findHataProposalServer.entities.responses.ProposalResponse;
import com.example.findHataProposalServer.exceptions.NoFoundProposalException;
import com.example.findHataProposalServer.exceptions.NoRightException;

import java.util.List;
import java.util.Map;

public interface ProposalsService {

    void addProposal(List<Role> roles, Proposal proposal) throws NoRightException;

    ProposalResponse getInfoAboutProposal(Integer proposalId) throws NoFoundProposalException;

    List<ShortInfoProposal> getAll();

    List<ShortInfoProposal> getAllWithKeyword(String keyword);

    void removeProposal(int proposalId, int userId) throws NoFoundProposalException, NoRightException;

    void removeProposal(List<Role> roles, int proposalId) throws NoRightException, NoFoundProposalException;

    void changeProposal(int proposalId, int userId, ChangeProposalRequest changes) throws NoFoundProposalException, NoRightException;

    void changeProposal(List<Role> roles, int proposalId, ChangeProposalRequest changes) throws NoFoundProposalException, NoRightException;

}
