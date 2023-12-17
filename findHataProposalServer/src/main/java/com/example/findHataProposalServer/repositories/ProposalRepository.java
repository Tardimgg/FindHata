package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.ProposalDB;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProposalRepository extends JpaRepository<ProposalDB, Integer>, JpaSpecificationExecutor<ProposalDB> {

    ProposalDB save(@NonNull ProposalDB proposalDB);

}
