package com.example.findHataProposalServer.algorithms.kdb;

import java.util.HashMap;

public class VectorRepImpl implements VectorRep {

    HashMap<Long, DataVector> map = new HashMap<>();
    private long maxId = 0;

    @Override
    public DataVector save(DataVector val) {
        map.put(maxId, val);
        val.setId(maxId);
        maxId++;

        return val;
    }

    @Override
    public DataVector findById(long id) {
        return map.get(id);
    }
}
