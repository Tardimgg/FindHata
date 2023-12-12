package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.entities.VectorizedFact;

public interface VectorRep {
    DataVector save(DataVector val);

    DataVector findById(long id);
}
