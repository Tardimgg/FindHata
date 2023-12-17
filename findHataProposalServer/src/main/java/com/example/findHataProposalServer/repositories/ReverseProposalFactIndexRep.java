package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.ProposalDB;
import com.example.findHataProposalServer.entities.ReverseProposalFactIndex;
import com.example.findHataProposalServer.entities.VectorizedFact;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReverseProposalFactIndexRep extends JpaRepository<ReverseProposalFactIndex, Long> {

    ReverseProposalFactIndex save(@NonNull ReverseProposalFactIndex v);

    List<ReverseProposalFactIndex> findAllByVectorizedFact(VectorizedFact fact);
    List<ReverseProposalFactIndex> findAllByProposalDB(ProposalDB fact);

}
