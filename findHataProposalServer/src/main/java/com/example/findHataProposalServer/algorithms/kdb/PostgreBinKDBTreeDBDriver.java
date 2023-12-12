package com.example.findHataProposalServer.algorithms.kdb;

import com.example.findHataProposalServer.entities.KDBNodeDB;
import com.example.findHataProposalServer.repositories.KDBNodeRep;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.Unsafe;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

@Component
public class PostgreBinKDBTreeDBDriver implements KDBTreeDBDriver {


    @Autowired
    private KDBNodeRep kdbNodeRep;

    PostgreBinKDBTreeDBDriver(KDBNodeRep kdbNodeRep) {
        this.kdbNodeRep = kdbNodeRep;
    }

    @Override
    public KDBNode getNode(long id) {
        KDBNodeDB node = kdbNodeRep.findById(id).orElse(null);

        return KDBNode.builder()
                .id(node.getId())
                .isLeaf(node.isLeaf())
                .vals(toDoubleArray(node.getVals()))
                .vectorOrNodesIds(toLongArray(node.getVectorOrNodesIds()))
                .splittingIndex(node.getSplittingIndex())
                .vecOrNodesLen(node.getVecOrNodesLen())
                .valsLen(node.getValsLen())
                .build();
    }

    private KDBNodeDB fromKDBNode(KDBNode node) {
        return KDBNodeDB.builder()
                .vals(toByteArray(node.getVals()))
                .vectorOrNodesIds(toByteArray(node.getVectorOrNodesIds()))
                .isLeaf(node.isLeaf())
                .splittingIndex(node.getSplittingIndex())
                .vecOrNodesLen(node.getVecOrNodesLen())
                .valsLen(node.getValsLen())
                .build();
    }


    @Override
    public long saveNode(KDBNode node) {
        KDBNodeDB nodeDB = fromKDBNode(node);

        KDBNodeDB savedNode = kdbNodeRep.save(nodeDB);
        return savedNode.getId();
    }

    @Override
    public long update(KDBNode node) {
        KDBNodeDB nodeDB = fromKDBNode(node);
        nodeDB.setId(node.getId());

        return kdbNodeRep.save(nodeDB).getId();
    }

    @Override
    public void removeNode(KDBNode node) {
        kdbNodeRep.deleteById(node.getId());
    }


    byte[] toByteArray(long[] obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length * Long.BYTES);
//        bb.order(ByteOrder.nativeOrder());
        bb.asLongBuffer().put(obj);
        return bb.array();
    }

    byte[] toByteArray(double[] obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length * Long.BYTES);
//        bb.order(ByteOrder.nativeOrder());
        bb.asDoubleBuffer().put(obj);
        return bb.array();
    }

    long[] toLongArray(byte[] obj) {
        int count = obj.length / 8;
        long[] longArray = new long[count];
        ByteBuffer byteBuffer = ByteBuffer.wrap(obj);
//        byteBuffer.order(ByteOrder.nativeOrder());
        for (int i = 0; i < count; i++) {
            longArray[i] = byteBuffer.getLong();
        }
        return longArray;
    }

    double[] toDoubleArray(byte[] obj) {
        int count = obj.length / 8;
        double[] longArray = new double[count];
        ByteBuffer byteBuffer = ByteBuffer.wrap(obj);
        for (int i = 0; i < count; i++) {
            longArray[i] = byteBuffer.getDouble();
        }
        return longArray;
    }
}
