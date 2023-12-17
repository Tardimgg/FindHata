package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.VectorizedFact;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VectorizedFactRep extends JpaRepository<VectorizedFact, Long> {

    VectorizedFact save(@NonNull VectorizedFact fact);

}