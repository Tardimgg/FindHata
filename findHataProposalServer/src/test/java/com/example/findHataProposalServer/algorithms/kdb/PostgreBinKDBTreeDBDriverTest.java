package com.example.findHataProposalServer.algorithms.kdb;

import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostgreBinKDBTreeDBDriverTest {

    @Test
    void testLong() {
        PostgreBinKDBTreeDBDriver driver = new PostgreBinKDBTreeDBDriver(null);


        long[] arr = new long[]{110, 65, 23, 474576, 3241234, 1234, 67567};
        byte[] bArr = driver.toByteArray(arr.clone());
        long[] res = driver.toLongArray(bArr);

        assertArrayEquals(arr, res);
    }

    @Test
    void testDouble() {
        PostgreBinKDBTreeDBDriver driver = new PostgreBinKDBTreeDBDriver(null);

        double[] arr = new double[]{12.124, 1425.235, 23.346, 53.234, 457.34536, 36364.34536, 4576457.6796899};
        byte[] bArr = driver.toByteArray(arr.clone());
        double[] res = driver.toDoubleArray(bArr);

        assertArrayEquals(arr, res);
    }

}