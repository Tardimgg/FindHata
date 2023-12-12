package com.example.findHataProposalServer.algorithms.kdb;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class KDBNode {

    @NonNull
    private long id;

    @NonNull
    private boolean isLeaf;

    @NonNull
    private int splittingIndex;

    private double[] vals;
    private int valsLen;

    private long[] vectorOrNodesIds;
    private int vecOrNodesLen;


    public static KDBNode emptyLeaf(int k, int splittingIndex) {
        return KDBNode.builder()
                .isLeaf(true)
                .splittingIndex(splittingIndex)
                .vals(new double[2 * k - 1])
                .valsLen(0)
                .vectorOrNodesIds(new long[2 * k - 1])
                .vecOrNodesLen(0)
                .build();
    }

}
