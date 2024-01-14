package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.KDBNodeDB;
import com.example.findHataProposalServer.entities.VectorizedFact;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface KDBNodeRep extends JpaRepository<KDBNodeDB, Long> {

    KDBNodeDB save(@NonNull KDBNodeDB node);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("from KDBNodeDB node where node.id = :id")
    KDBNodeDB findByIdForShare(@NonNull long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("from KDBNodeDB node where node.id = :id")
    KDBNodeDB findByIdForUpdate(@NonNull long id);

}
