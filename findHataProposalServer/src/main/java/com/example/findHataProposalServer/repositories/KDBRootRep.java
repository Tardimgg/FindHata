package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.KDBNodeDB;
import com.example.findHataProposalServer.entities.KDBRoot;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KDBRootRep extends JpaRepository<KDBRoot, Long> {

    KDBRoot save(@NonNull KDBRoot root);

}
