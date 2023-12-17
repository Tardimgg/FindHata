package com.example.findHataProposalServer.algorithms.kdb;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.*;

public class KDBTree {

    private int vectorSize;
    private KDBTreeDBDriver dbDriver;
    private KDBNode root;
    private VectorRep vectorRep;

    private int k = 3;

    public KDBTree(int nodeSize, int k, KDBTreeDBDriver dbDriver, VectorRep vectorRep, KDBNode root) {
        this.k = k;
        this.vectorSize = nodeSize;
        this.dbDriver = dbDriver;
        this.vectorRep = vectorRep;
        this.root = root;
    }

    public KDBTree(int nodeSize, KDBTreeDBDriver dbDriver, VectorRep vectorRep, KDBNode root) {
        this(nodeSize, 3, dbDriver,vectorRep, root);
    }

    private KDBNode copyFrom(KDBNode node, int offset, int countValues, int countVectorOrNodes) {
        double[] values = new double[2 * k - 1];
        for (int i = offset; i < offset + countValues; ++i) {
            values[i - offset] = node.getVals()[i];
        }

        long[] next = new long[2 * k - (node.isLeaf() ? 1 : 0)];
        for (int i = offset; i < offset + countVectorOrNodes; ++i) {
            next[i - offset] = node.getVectorOrNodesIds()[i];
        }

        return KDBNode.builder()
                .isLeaf(node.isLeaf())
//                .splittingIndex((node.getSplittingIndex() + 1) % vectorSize)
                .splittingIndex(node.getSplittingIndex())
                .vals(values)
                .vectorOrNodesIds(next)
                .valsLen(countValues)
                .vecOrNodesLen(countVectorOrNodes)
                .build();
    }

    NewNode naiveSplitNode(KDBNode node) {
        double splitter = node.getVals()[k - 1]; // тут проблема !!!!!!! элемент справа не всегда больше !!!!!

        KDBNode lNode = copyFrom(node, 0, k - (!node.isLeaf() ? 1 : 0), k);
        KDBNode rNode = copyFrom(node, k, k - 1, k - (node.isLeaf() ? 1 : 0));

        return NewNode.builder()
                .left(lNode)
                .right(rNode)
                .splitterIndex(node.getSplittingIndex())
                .splitter(splitter)
                .build();
    }
    
    private KDBNode generateChild(KDBNode node) {
        return KDBNode.builder()
                .vectorOrNodesIds(new long[2 * k - (node.isLeaf() ? 1 : 0)])
                .vals(new double[2 * k - 1])
                .splittingIndex(node.getSplittingIndex())
//                .splittingIndex(splitterIndex)
                .isLeaf(node.isLeaf())
                .valsLen(0)
                .vecOrNodesLen(0)
                .build();
    }
    
    private void saveOnLeaf(KDBNode leaf, long nextId, double val) {
        int prevLen = leaf.getVecOrNodesLen();
        leaf.getVectorOrNodesIds()[prevLen] = nextId;
        leaf.setVecOrNodesLen(prevLen + 1);

        prevLen = leaf.getValsLen();
        leaf.getVals()[prevLen] = val;
        leaf.setValsLen(prevLen + 1);
    }

    private void saveOnNode(KDBNode node, long nextId, double val) {
        int prevLen = node.getVecOrNodesLen();
        node.getVectorOrNodesIds()[prevLen] = nextId;
        node.setVecOrNodesLen(prevLen + 1);

        prevLen = node.getValsLen();
        if (prevLen < 2 * k - 1) {
            node.getVals()[prevLen] = val;
            node.setValsLen(prevLen + 1);
        }
    }

    private void saveAllOnNode(KDBNode node, KDBNode rootNode, List<IntLong> newChildren) {
        for (int i = 0; i < newChildren.size() - 1; i++) {
            IntLong child = newChildren.get(i);
            saveOnNode(node, child.l, rootNode.getVals()[child.i]);
        }
        if (newChildren.size() == 1) {
            KDBNode emptyLeaf = KDBNode.emptyLeaf(k,
                    (node.getSplittingIndex() + 1) % vectorSize);
            long savedLeafId = dbDriver.saveNode(emptyLeaf);

            if (newChildren.get(0).i == 0) {
                int prevLen = node.getVecOrNodesLen();
                node.getVectorOrNodesIds()[prevLen] = newChildren.get(newChildren.size() - 1).l;
                node.setVecOrNodesLen(++prevLen);

                node.getVectorOrNodesIds()[prevLen] = savedLeafId;
                node.setVecOrNodesLen(prevLen + 1);

                prevLen = node.getValsLen();
                node.getVals()[prevLen] = rootNode.getVals()[newChildren.get(0).i];
                node.setValsLen(prevLen + 1);
            } else {
                int prevLen = node.getVecOrNodesLen();
                node.getVectorOrNodesIds()[prevLen] = savedLeafId;
                node.setVecOrNodesLen(++prevLen);

                node.getVectorOrNodesIds()[prevLen] = newChildren.get(newChildren.size() - 1).l;
                node.setVecOrNodesLen(++prevLen);

                prevLen = node.getValsLen();
                node.getVals()[prevLen] = rootNode.getVals()[newChildren.get(0).i - 1];
                node.setValsLen(prevLen + 1);
            }

        } else if (newChildren.size() != 0){
            int prevLen = node.getVecOrNodesLen();
            node.getVectorOrNodesIds()[prevLen] = newChildren.get(newChildren.size() - 1).l;
            node.setVecOrNodesLen(prevLen + 1);
        }
    }

    double mid(double f, double s) {
        return f + ((s - f) / 2);
    }

    Optional<Double> findMedian(KDBNode node, int splittingIndex) {
        double[] baseVector = new double[vectorSize];
        double[] delta = new double[vectorSize];
        Arrays.fill(delta, 2);
        double l = -1.1;
        double r = 1.1;
        while (l + 0.00000000001 < r) {
            double mid = mid(l, r);

            baseVector[splittingIndex] = mid(-1.1, mid);
            delta[splittingIndex] = mid - baseVector[splittingIndex];

            Quantity quantity = countNumber(node, baseVector, delta);
            long onLeft = quantity.counter;
            long onRight = quantity.all - onLeft;

            baseVector[splittingIndex] = 0;
            delta[splittingIndex] = 2;

            if (Math.abs(onRight - onLeft) <= 1) {
                return Optional.of(mid);
            }
            if (onLeft < onRight) {
                l = mid;
            } else {
                r = mid;
            }
        }
        return Optional.empty();
    }

    // adapter will not be saved
    Optional<NewNode> splitNodeBySplitter(KDBNode node, int splitterIndex) {
        Double median = findMedian(node, splitterIndex).orElse(null);
        if (median != null) {
            NewNode res = splitNodeHierarchyBySplitter(node.getId(), splitterIndex, median);
            return Optional.of(res);
        }
        return Optional.empty();
    }

    // adapter will not be saved
    NewNode splitNode(KDBNode node) {
        int curSplitterIndex = (node.getSplittingIndex() + 1) % vectorSize;
        do {
            NewNode res = splitNodeBySplitter(node, curSplitterIndex).orElse(null);
            if (res != null) {
                return res;
            }
            curSplitterIndex++;
            curSplitterIndex %= vectorSize;
        } while (curSplitterIndex != node.getSplittingIndex() + 1);

        throw new RuntimeException(":(");
    }

	KDBNode normalizeNode(KDBNode node) {
		if (node.getValsLen() == 2 * k - 1) {
            NewNode res = splitNode(node);
            KDBNode adapter = generateAdapter(res.splitter,
                    res.left.getId(),
                    res.right.getId(),
                    res.splitterIndex
            );
            adapter.setId(dbDriver.saveNode(adapter));
            return adapter;
		}
		return node;
	}

    NewNode splitNodeHierarchyBySplitter(long nodeId, int splitterIndex, double splitter) {
        KDBNode node = dbDriver.getNode(nodeId);

        KDBNode lNode = generateChild(node);
        KDBNode rNode = generateChild(node);
        
        if (node.isLeaf()) {
            for (int i = 0; i < node.getVecOrNodesLen(); i++) {
                long vecId = node.getVectorOrNodesIds()[i];
                DataVector vector = vectorRep.findById(vecId);
                if (vector.getVector()[splitterIndex] <= splitter) {
                    saveOnLeaf(lNode, vecId, node.getVals()[i]);
                } else {
                    saveOnLeaf(rNode, vecId, node.getVals()[i]);
                }
            }
        } else {
            int vecNodeLen = node.getVecOrNodesLen();

            List<IntLong> leftId = new ArrayList<>();
            List<IntLong> rightId = new ArrayList<>();
            for (int i = 0; i < vecNodeLen; i++) {

                NewNode newNode = splitNodeHierarchyBySplitter(node.getVectorOrNodesIds()[i],
                        splitterIndex, splitter);

                node = dbDriver.getNode(nodeId); // for gc

                if (newNode.left != null) {
                    leftId.add(new IntLong(i, newNode.left.getId()));
                }
                if (newNode.right != null) {
                    rightId.add(new IntLong(i, newNode.right.getId()));
                }
            }
            saveAllOnNode(lNode, node, leftId);
            saveAllOnNode(rNode, node, rightId);
        }

        node = dbDriver.getNode(nodeId); // for gc
        dbDriver.removeNode(node);

        var builder = NewNode.builder()
                .splitterIndex(splitterIndex)
                .splitter(splitter);
        if (lNode.getValsLen() > 0) {
            lNode.setId(dbDriver.saveNode(lNode));
			lNode = normalizeNode(lNode);
            builder.left(lNode);

        }
        if (rNode.getValsLen() > 0) {
            rNode.setId(dbDriver.saveNode(rNode));
			rNode = normalizeNode(rNode);
            builder.right(rNode);
        }
        return builder.build();
    }

    int findLeftBorder(double[] data, int len, double val) {
        int l = -1;
        int r = len;

        while (l + 1 < r) {
            int mid = l + ((r - l) >> 1);

            if (data[mid] < val) {
                l = mid;
            } else {
                r = mid;
            }
        }
        return r;
    }

    int findRightBorder(double[] data, int len, double val) {
        int l = 0;
        int r = len;

        while (l + 1 < r) {
            int mid = l + ((r - l) >> 1);

            if (data[mid] <= val) {
                l = mid;
            } else {
                r = mid;
            }
        }
        if (data[l] < val) {
            return Integer.min(r, l + 1);
        }
        return l;
    }


    KDBNode generateAdapter(double splitter, long leftId, long rightId, int splitterIndex) {
        double[] vals = new double[2 * k - 1];
        vals[0] = splitter;

        long[] vecOrNodes = new long[2 * k];
        vecOrNodes[0] = leftId;
        vecOrNodes[1] = rightId;

        return KDBNode.builder()
                .isLeaf(false)
                .vals(vals)
                .valsLen(1)
                .vectorOrNodesIds(vecOrNodes)
                .vecOrNodesLen(2)
                .splittingIndex(splitterIndex)
                .build();
    }

    private static void rightShift(long[] arr, int from, int to) {
        for (int i = to; i >= from + 1; i--) {
            arr[i] = arr[i - 1];
        }
    }

    private static void rightShift(double[] arr, int from, int to) {
        for (int i = to; i >= from + 1; i--) {
            arr[i] = arr[i - 1];
        }
    }

    private static void leftShift(long[] arr, int from, int to) {
        for (int i = from - 1; i < to; i++) {
            arr[i] = arr[i + 1];
        }
    }

    private static void leftShift(double[] arr, int from, int to) {
        for (int i = from - 1; i < to; i++) {
            arr[i] = arr[i + 1];
        }
    }


    private InsertResp insert(long currentNodeId, double[] vector) {
        KDBNode currentNode = dbDriver.getNode(currentNodeId);
        double vectorVal = vector[currentNode.getSplittingIndex()];


        int index = findLeftBorder(currentNode.getVals(), currentNode.getValsLen(), vectorVal);

        if (currentNode.isLeaf()) {
            while (index < currentNode.getValsLen() &&
                    vector[currentNode.getSplittingIndex()] == currentNode.getVals()[index]) {
                DataVector vec = vectorRep.findById(currentNode.getVectorOrNodesIds()[index]);
                if (Arrays.equals(vec.getVector(), vector)) {
                    return new InsertResp(vec.getId(), false);
                }
                index++;
            }

            rightShift(currentNode.getVals(), index, currentNode.getValsLen());
            currentNode.setValsLen(currentNode.getValsLen() + 1);
            currentNode.getVals()[index] = vectorVal;

            rightShift(currentNode.getVectorOrNodesIds(), index, currentNode.getVecOrNodesLen());
            currentNode.setVecOrNodesLen(currentNode.getVecOrNodesLen() + 1);
            DataVector dataVector = new DataVector(vector);
            dataVector = vectorRep.save(dataVector);
            currentNode.getVectorOrNodesIds()[index] = dataVector.getId();

            dbDriver.update(currentNode);

            return new InsertResp(dataVector.getId(), currentNode.getValsLen() == k * 2 - 1);

        } else {
            KDBNode child = dbDriver.getNode(currentNode.getVectorOrNodesIds()[index]);
            InsertResp resp = insert(child.getId(), vector);

            currentNode = dbDriver.getNode(currentNodeId); // update node
            if (resp.filled) {

                NewNode newNode = splitNodeBySplitter(child, currentNode.getSplittingIndex()).orElse(null);
                currentNode = dbDriver.getNode(currentNodeId); // for gc
                if (newNode == null) {
                    NewNode res = splitNode(child);
                    KDBNode adapter = generateAdapter(res.splitter,
                            res.left.getId(),
                            res.right.getId(),
                            res.splitterIndex);
                    currentNode.getVectorOrNodesIds()[index] = dbDriver.saveNode(adapter);
                } else {
                    rightShift(currentNode.getVals(), index, currentNode.getValsLen());
                    currentNode.setValsLen(currentNode.getValsLen() + 1);
                    currentNode.getVals()[index] = newNode.splitter;

                    rightShift(currentNode.getVectorOrNodesIds(), index, currentNode.getVecOrNodesLen());
                    currentNode.setVecOrNodesLen(currentNode.getVecOrNodesLen() + 1);


                    currentNode.getVectorOrNodesIds()[index] = newNode.left.getId();
                    currentNode.getVectorOrNodesIds()[index + 1] = newNode.right.getId();
                }

                dbDriver.update(currentNode);

                return new InsertResp(resp.vectorId, currentNode.getValsLen() == k * 2 - 1);
            }
            return new InsertResp(resp.vectorId, false);
        }
    }


    public long insert(double[] vector) {
        if (root == null) {
            long vectorId = vectorRep.save(new DataVector(vector)).getId();

            double[] vals = new double[2 * k - 1];
            vals[0] = vector[0];
            long[] vectorIds = new long[2 * k - 1];
            vectorIds[0] = vectorId;

            root = KDBNode.builder()
                    .isLeaf(true)
                    .splittingIndex(0)
                    .vals(vals)
                    .vectorOrNodesIds(vectorIds)
                    .valsLen(1)
                    .vecOrNodesLen(1)
                    .build();

            root.setId(dbDriver.saveNode(root));
            dbDriver.updateRoot(root);
            return vectorId;
        } else {
            InsertResp resp = insert(root.getId(), vector);
            root = dbDriver.getNode(root.getId());
            if (resp.filled) {
                int splittingIndex = normalize((root.getSplittingIndex() - 1) % vectorSize, vectorSize);
                int count = 0;
                while (true) {
                    if (count > vectorSize) {
                        throw new RuntimeException("infinite loop");
                    }
                    Double splitter = findMedian(root, splittingIndex).orElse(null);
                    if (splitter == null) {
                        splittingIndex--;
                        splittingIndex = normalize(splittingIndex, vectorSize);
                    } else {
                        dbDriver.forgetRoot();
                        NewNode newRoot = splitNodeHierarchyBySplitter(root.getId(),
                                splittingIndex, splitter);

                        root = generateAdapter(splitter,
                                newRoot.left.getId(),
                                newRoot.right.getId(),
                                splittingIndex);

                        root.setId(dbDriver.saveNode(root));
                        dbDriver.updateRoot(root);
                        return resp.vectorId;
                    }
                    count++;
                }
            }
            return resp.vectorId;
        }
    }

    private static int normalize(int i, int r) {
        if (i < 0) {
            i += r;
        }
        return i;
    }

    private Segment findSegmentToSearch(KDBNode node, double[] baseVector, double[] deltaVector) {
        int splittingIndex = node.getSplittingIndex();

        double minVal = baseVector[splittingIndex] - deltaVector[splittingIndex];
        int minIndex = findLeftBorder(node.getVals(), node.getValsLen(), minVal);

        double maxVal = baseVector[splittingIndex] + deltaVector[splittingIndex];
        int maxIndex = findRightBorder(node.getVals(), node.getValsLen(), maxVal);
        return new Segment(minIndex, maxIndex);
    }


    private List<KDBNode> findLeaves(long currentNodeId, double[] baseVector, double[] deltaVector) {
        KDBNode currentNode = dbDriver.getNode(currentNodeId);

        List<KDBNode> ans = new ArrayList<>();
        if (currentNode.isLeaf() && currentNode.getVecOrNodesLen() != 0) {
            ans.add(currentNode);
        } else if (currentNode.getVecOrNodesLen() != 0){
            Segment segment = findSegmentToSearch(currentNode, baseVector, deltaVector);
            int minIndex = segment.l;
            int maxIndex = segment.r;
            for (int i = minIndex; i <= maxIndex; i++) {
                ans.addAll(findLeaves(currentNode.getVectorOrNodesIds()[i], baseVector, deltaVector));
                currentNode = dbDriver.getNode(currentNodeId); // for gc
            }
        }
        return ans;
    }

    boolean traceRoute(KDBNode cur, long id) {
        if (cur.isLeaf()) {
            for (long next: cur.getVectorOrNodesIds()) {
                if (next == id) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < cur.getVecOrNodesLen(); i++) {
                long next = cur.getVectorOrNodesIds()[i];
                if (traceRoute(dbDriver.getNode(next), id)) {
                    double lessThan = Double.MIN_VALUE;
                    if (i != 0) {
                        lessThan = cur.getVals()[i - 1];
                    }

                    double moreThan = Double.MAX_VALUE;
                    if (i < cur.getValsLen()) {
                        moreThan = cur.getVals()[i];
                    }
                    double[] forPrint = vectorRep.findById(id).getVector();
                    System.out.println(lessThan + " < " + forPrint[cur.getSplittingIndex()]
                            + " < " + moreThan + " :" + cur.getSplittingIndex() + " : id=" + i);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean remove(long id) {
        if (root != null) {
            double[] delta = new double[vectorSize];
            double[] baseVector = vectorRep.findById(id).getVector();
            List<KDBNode> leaves = findLeaves(root.getId(), baseVector, delta);
            for (KDBNode leaf : leaves) {
                Segment segment = findSegmentToSearch(leaf, baseVector, delta);
                int minIndex = segment.l;
                while (minIndex < leaf.getVecOrNodesLen() &&
                        Math.abs(baseVector[leaf.getSplittingIndex()] - leaf.getVals()[minIndex]) <= delta[0]) {
                    if (leaf.getVectorOrNodesIds()[minIndex] == id) {
                        vectorRep.remove(leaf.getVectorOrNodesIds()[minIndex]);
                        leftShift(leaf.getVals(), minIndex + 1, leaf.getValsLen() - 1);
                        leftShift(leaf.getVectorOrNodesIds(), minIndex + 1, leaf.getVecOrNodesLen() - 1);
                        leaf.setValsLen(leaf.getValsLen() - 1);
                        leaf.setVecOrNodesLen(leaf.getVecOrNodesLen() - 1);
                        dbDriver.update(leaf);
                        return true;
                    } else {
                        minIndex++;
                    }
                }
            }
        }
//        throw new RuntimeException("remove err: " + id);
        return false;
    }

    public Quantity countNumber(KDBNode curNode, double[] baseVector, double[] deltaVector) {
        if (curNode != null) {
            List<KDBNode> leaves = findLeaves(curNode.getId(), baseVector, deltaVector);

            long count = 0;
            long all = 0;
            for (KDBNode leaf : leaves) {
                Segment segment = findSegmentToSearch(leaf, baseVector, deltaVector);
                int minIndex = segment.l;
                int maxIndex = segment.r;

                for (int i = minIndex; i <= Integer.min(maxIndex, leaf.getVecOrNodesLen() - 1); i++) {
                    DataVector vec = vectorRep.findById(leaf.getVectorOrNodesIds()[i]);
                    boolean fits = true;
                    for (int j = 0; j < baseVector.length; j++) {
                        double vecVal = vec.getVector()[j];
                        if (vecVal < baseVector[j] - deltaVector[j] || vecVal > baseVector[j] + deltaVector[j]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        count++;
                    }
                    all++;
                }
            }
            return new Quantity(count, all);
        }
        return new Quantity(0, 0);
    }

    public List<Long> findImpl(KDBNode curNode, double[] baseVector, double[] deltaVector) {
        if (curNode != null) {
            List<KDBNode> leaves = findLeaves(curNode.getId(), baseVector, deltaVector);

            List<Long> ids = new ArrayList<>();
            for (KDBNode leaf : leaves) {
                Segment segment = findSegmentToSearch(leaf, baseVector, deltaVector);
                int minIndex = segment.l;
                int maxIndex = segment.r;

                for (int i = minIndex; i <= Integer.min(maxIndex, leaf.getVecOrNodesLen() - 1); i++) {
                    DataVector vec = vectorRep.findById(leaf.getVectorOrNodesIds()[i]);
                    boolean fits = true;
                    for (int j = 0; j < baseVector.length; j++) {
                        double vecVal = vec.getVector()[j];
                        if (vecVal < baseVector[j] - deltaVector[j] || vecVal > baseVector[j] + deltaVector[j]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        ids.add(leaf.getVectorOrNodesIds()[i]);
                    }
                }
            }
            return ids;
        }
        return new ArrayList<>(0);
    }

    public List<Long> find(double[] baseVector, double[] deltaVector) {
        return findImpl(root, baseVector, deltaVector);
    }


    @AllArgsConstructor
    static class IntLong {
        int i;
        long l;
    }

    @Builder
    static class NewNode {

        double splitter;
        int splitterIndex;

        KDBNode left;
        KDBNode right;

    }

    @AllArgsConstructor
    static class InsertResp {

        long vectorId;
        boolean filled;
    }

    @AllArgsConstructor
    static class Segment {

        int l;
        int r;
    }

    @AllArgsConstructor
    static class Quantity {

        long counter;
        long all;
    }
}
