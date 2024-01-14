package com.example.findHataProposalServer.algorithms.kdb;

import java.nio.ByteBuffer;

public class ByteTransform {

    public static byte[] toByteArray(long[] obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length * Long.BYTES);
//        bb.order(ByteOrder.nativeOrder());
        bb.asLongBuffer().put(obj);
        return bb.array();
    }

    public static byte[] toByteArray(double[] obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length * Long.BYTES);
//        bb.order(ByteOrder.nativeOrder());
        bb.asDoubleBuffer().put(obj);
        return bb.array();
    }

    public static long[] toLongArray(byte[] obj) {
        int count = obj.length / 8;
        long[] longArray = new long[count];
        ByteBuffer byteBuffer = ByteBuffer.wrap(obj);
//        byteBuffer.order(ByteOrder.nativeOrder());
        for (int i = 0; i < count; i++) {
            longArray[i] = byteBuffer.getLong();
        }
        return longArray;
    }

    public static double[] toDoubleArray(byte[] obj) {
        int count = obj.length / 8;
        double[] longArray = new double[count];
        ByteBuffer byteBuffer = ByteBuffer.wrap(obj);
        for (int i = 0; i < count; i++) {
            longArray[i] = byteBuffer.getDouble();
        }
        return longArray;
    }
}
