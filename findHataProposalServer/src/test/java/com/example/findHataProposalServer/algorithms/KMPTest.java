package com.example.findHataProposalServer.algorithms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KMPTest {

    @Test
    void test() {
        assertEquals(KMP.solve("aaa", "a").size(), 3);
    }

    @Test
    void test1() {
        assertEquals(KMP.solve("HELLO", "eL").size(), 0);
    }

    @Test
    void test2() {
        assertEquals(KMP.solve("hello my friend hello", "he").size(), 2);
    }

}