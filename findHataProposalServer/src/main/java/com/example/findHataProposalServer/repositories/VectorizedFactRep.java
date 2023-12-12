package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.ProposalBD;
import com.example.findHataProposalServer.entities.VectorizedFact;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VectorizedFactRep extends JpaRepository<VectorizedFact, Long> {

    VectorizedFact save(@NonNull VectorizedFact fact);

}