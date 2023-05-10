package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.ProposalBD;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProposalRepository extends JpaRepository<ProposalBD, Integer>, JpaSpecificationExecutor<ProposalBD> {

    ProposalBD save(@NonNull ProposalBD proposalBD);

}
