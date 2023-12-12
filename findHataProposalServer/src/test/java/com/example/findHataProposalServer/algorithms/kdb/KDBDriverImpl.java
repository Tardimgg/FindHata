package com.example.findHataProposalServer.algorithms.kdb;

import java.util.HashMap;

public class KDBDriverImpl implements KDBTreeDBDriver {

    HashMap<Long, KDBNode> map = new HashMap<>();
    private long maxId = 0;

    @Override
    public KDBNode getNode(long id) {
        return map.get(id);
    }

    @Override
    public long saveNode(KDBNode node) {
        node.setId(maxId);
        long prevMaxId = maxId;
        maxId++;

        map.put(prevMaxId, node);

        return prevMaxId;
    }

    @Override
    public long update(KDBNode node) {
        map.put(node.getId(), node);
        return node.getId();
    }

    @Override
    public void removeNode(KDBNode node) {
        map.remove(node.getId());
    }
}
