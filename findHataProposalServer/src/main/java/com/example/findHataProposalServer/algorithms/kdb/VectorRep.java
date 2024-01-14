package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.entities.VectorizedFact;

import java.util.List;

public interface VectorRep {
    DataVector save(DataVector val);

    DataVector findById(long id);

    List<DataVector> findByIds(Iterable<Long> id);

    void remove(long id);
}
