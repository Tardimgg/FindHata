package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.entities.VectorizedFact;
import com.example.findHataProposalServer.repositories.VectorizedFactRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostgreVectorRep implements VectorRep {

    @Autowired
    private VectorizedFactRep factRep;

    @Override
    public DataVector save(DataVector val) {
        VectorizedFact fact = factRep.save(VectorizedFact.builder().vector(val.getVector()).build());
        return new DataVector(fact.getId(), fact.getVector());
    }

    @Override
    public DataVector findById(long id) {
        VectorizedFact fact = factRep.findById(id).orElse(null);
        if (fact == null) {
            return null;
        }
        return new DataVector(fact.getId(), fact.getVector());
    }

    @Override
    public void remove(long id) {
        factRep.deleteById(id);
    }
}
