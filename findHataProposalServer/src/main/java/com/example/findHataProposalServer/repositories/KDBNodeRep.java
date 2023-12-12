package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.KDBNodeDB;
import com.example.findHataProposalServer.entities.VectorizedFact;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KDBNodeRep extends JpaRepository<KDBNodeDB, Long> {

    KDBNodeDB save(@NonNull KDBNodeDB node);

}
