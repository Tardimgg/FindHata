package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.algorithms.kdb.KDBNode;

public interface KDBTreeDBDriver {

    KDBNode getNode(long id);

    long saveNode(KDBNode node);

    void updateRoot(KDBNode newRoot);
    KDBNode getRoot();
    void forgetRoot();

    long update(KDBNode node);

    void removeNode(KDBNode node);
}
