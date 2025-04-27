package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.algorithms.kdb.KDBNode;

public interface KDBTreeDBDriver {

    KDBNode getNode(long id);

    Lock readLock(long id);
//    void cancelReadLock(long id);
    Lock writeLock(long id);
//    void cancelWriteLock(long id);

    long saveNode(KDBNode node);

    void updateRoot(KDBNode newRoot);
    KDBNode getRoot();
    void forgetRoot();

    long update(KDBNode node);

    void removeNode(KDBNode node);
}
