package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.algorithms.kdb.KDBNode;

public interface KDBTreeDBDriver {

    KDBNode getNode(long id);

    long saveNode(KDBNode node);

    long update(KDBNode node);

    void removeNode(KDBNode node);
}
