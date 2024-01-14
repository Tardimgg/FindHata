package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.entities.KDBNodeDB;
import com.example.findHataProposalServer.entities.KDBRoot;
import com.example.findHataProposalServer.repositories.KDBNodeRep;
import com.example.findHataProposalServer.repositories.KDBRootRep;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.Unsafe;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.List;

@Component
public class PostgreBinKDBTreeDBDriver implements KDBTreeDBDriver {


    @Autowired
    private KDBNodeRep kdbNodeRep;

    @Autowired
    private KDBRootRep kdbRootRep;

    PostgreBinKDBTreeDBDriver(KDBNodeRep kdbNodeRep) {
        this.kdbNodeRep = kdbNodeRep;
    }

    @Override
    public KDBNode getNode(long id) {
        KDBNodeDB node = kdbNodeRep.findById(id).orElse(null);
        if (node == null) {
            return null;
        }

        return fromKDBNodeDB(node);
    }

    /*
    @Override
    @Transactional
    public KDBNode getNodeWithShareLock(long id) {
        KDBNodeDB node = kdbNodeRep.findByIdForShare(id);
        if (node == null) {
            return null;
        }

        return fromKDBNodeDB(node);
    }


    @Override
    @Transactional
    public KDBNode getNodeWithUpdateLock(long id) {
        KDBNodeDB node = kdbNodeRep.findByIdForUpdate(id);
        if (node == null) {
            return null;
        }

        return fromKDBNodeDB(node);
    }

     */


    private KDBNode fromKDBNodeDB(KDBNodeDB node) {
        return KDBNode.builder()
                .id(node.getId())
                .isLeaf(node.isLeaf())
                .vals(ByteTransform.toDoubleArray(node.getVals()))
                .vectorOrNodesIds(ByteTransform.toLongArray(node.getVectorOrNodesIds()))
                .splittingIndex(node.getSplittingIndex())
                .vecOrNodesLen(node.getVecOrNodesLen())
                .valsLen(node.getValsLen())
                .build();
    }

    private KDBNodeDB fromKDBNode(KDBNode node) {
        return KDBNodeDB.builder()
                .id(node.getId())
                .vals(ByteTransform.toByteArray(node.getVals()))
                .vectorOrNodesIds(ByteTransform.toByteArray(node.getVectorOrNodesIds()))
                .isLeaf(node.isLeaf())
                .splittingIndex(node.getSplittingIndex())
                .vecOrNodesLen(node.getVecOrNodesLen())
                .valsLen(node.getValsLen())
                .build();
    }


    @Override
    public long saveNode(KDBNode node) {
        KDBNodeDB nodeDB = fromKDBNode(node);
        if (nodeDB.getId() != 0) {
            throw new RuntimeException("id when creating node != 0");
        }

        KDBNodeDB savedNode = kdbNodeRep.save(nodeDB);
        return savedNode.getId();
    }

    @Override
//    @Transactional
    public void updateRoot(KDBNode newRoot) {
        KDBNodeDB nodeDB = fromKDBNode(newRoot);

        kdbRootRep.deleteAll();
        kdbRootRep.save(new KDBRoot(nodeDB));
    }

    @Override
    public KDBNode getRoot() {
        List<KDBRoot> root = kdbRootRep.findAll();
        if (root.size() == 0) {
            return null;
        } else if (root.size() != 1) {
            throw new RuntimeException("db contains > 1 roots");
        }
        return fromKDBNodeDB(root.get(0).getKdbNodeDB());
    }

    @Override
//    @Transactional
    public void forgetRoot() {
        kdbRootRep.deleteAll();
    }

    @Override
    public long update(KDBNode node) {
        KDBNodeDB nodeDB = fromKDBNode(node);

        return kdbNodeRep.save(nodeDB).getId();
    }

    @Override
    public void removeNode(KDBNode node) {
        kdbNodeRep.deleteById(node.getId());
    }
}
