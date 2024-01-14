package com.example.findHataProposalServer.algorithms.kdb;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    @Override
    public List<DataVector> findByIds(Iterable<Long> id) {
        return StreamSupport.stream(id.spliterator(), false).map(this::findById).collect(Collectors.toList());
    }

    @Override
    public void remove(long id) {
        map.remove(id);
    }
}
