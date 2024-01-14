package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.entities.VectorizedFact;
import com.example.findHataProposalServer.repositories.VectorizedFactRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostgreVectorRep implements VectorRep {

    @Autowired
    private VectorizedFactRep factRep;

    @Override
    public DataVector save(DataVector val) {
        VectorizedFact fact = factRep.save(VectorizedFact.builder().vector(ByteTransform.toByteArray(val.getVector())).build());
        return new DataVector(fact.getId(), ByteTransform.toDoubleArray(fact.getVector()));
    }

    @Override
    public DataVector findById(long id) {
        VectorizedFact fact = factRep.findById(id).orElse(null);
        if (fact == null) {
            return null;
        }
        return new DataVector(fact.getId(), ByteTransform.toDoubleArray(fact.getVector()));
    }

    @Override
    public List<DataVector> findByIds(Iterable<Long> id) {
        List<VectorizedFact> facts = factRep.findAllById(id);

        return facts.stream().map((fact) ->
                new DataVector(fact.getId(), ByteTransform.toDoubleArray(fact.getVector())))
                .collect(Collectors.toList());
    }

    @Override
    public void remove(long id) {
        factRep.deleteById(id);
    }
}
