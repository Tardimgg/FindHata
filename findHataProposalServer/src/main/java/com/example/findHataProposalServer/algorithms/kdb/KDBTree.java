package com.example.findHataProposalServer.algorithms.kdb;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    NewNode splitNode(KDBNode node) {
        double splitter = node.getVals()[k - 1];

        KDBNode lNode = copyFrom(node, 0, k - (!node.isLeaf() ? 1 : 0), k);
        KDBNode rNode = copyFrom(node, k, k - 1, k - (node.isLeaf() ? 1 : 0));

        return NewNode.builder()
                .left(lNode)
                .right(rNode)
                .splitter(splitter)
                .build();
    }
    
    private KDBNode generateChild(KDBNode node) {
        return KDBNode.builder()
                .vectorOrNodesIds(new long[2 * k - (node.isLeaf() ? 1 : 0)])
                .vals(new double[2 * k - 1])
                .splittingIndex(node.getSplittingIndex())
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

	KDBNode normalizeNode(KDBNode node) {
		if (node.getValsLen() == 2 * k - 1) {
			NewNode res = splitNode(node);
			KDBNode adapter = generateAdapter(res.splitter,
				dbDriver.saveNode(res.left),
				dbDriver.saveNode(res.right),
				res.left.getSplittingIndex()
			);

			dbDriver.removeNode(node);
			node = adapter;
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
                .splitter(splitter);
        if (lNode.getValsLen() > 0) {
			lNode = normalizeNode(lNode);
            lNode.setId(dbDriver.saveNode(lNode));
            builder.left(lNode);

        }
        if (rNode.getValsLen() > 0) {
			rNode = normalizeNode(rNode);
            rNode.setId(dbDriver.saveNode(rNode));
            builder.right(rNode);
        }
        return builder.build();
    }

    int findIndex(double[] data, int len, double val) {
        int l = 0;
        int r = len;

        while (l + 1 < r) {
            int mid = l + ((r - l) >> 1);

            if (data[mid] <= val) {
                l = mid;

            } else if (data[mid] > val) {
                r = mid;
            }
        }

        if (data[l] < val) {
            return Integer.min(r, l + 1);
        }
        return l;
    }


    KDBNode generateAdapter(double splitter, long leftId, long rightId, int splittingIndex) {
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
                .splittingIndex(splittingIndex)
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


    private InsertResp insert(long currentNodeId, double[] vector) {
        KDBNode currentNode = dbDriver.getNode(currentNodeId);
        double vectorVal = vector[currentNode.getSplittingIndex()];

        int index = findIndex(currentNode.getVals(), currentNode.getValsLen(), vectorVal);

        if (currentNode.isLeaf()) {
            if (index < currentNode.getValsLen()) {
                DataVector vec = vectorRep.findById(currentNode.getVectorOrNodesIds()[index]);
                if (Arrays.equals(vec.getVector(), vector)) {
                    return new InsertResp(vec.getId(), false);
                }
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

                double[] borders = currentNode.getVals();
                double midBorder;
                if (index == 0) {
                    Random random = new Random();

                    double minVal;
                    if (borders[0] > 0) {
                        minVal = -0.1;
                    } else if (borders[0] > -0.4) {
                        minVal = -0.5;
                    } else {
                        minVal = 1;
                    }

                    midBorder = minVal + random.nextDouble() * (borders[0] - minVal);
                    midBorder = Double.max(midBorder, minVal + 0.001);
//                    if (currentNode.getValsLen() == 1) {
//                        midBorder = borders[0] - Double.max(borders[0] / 2, 0.05);
//                    } else {
//                        midBorder = borders[0] - ((borders[1] - borders[0]) / 2);
//                    }
                } else if (index == currentNode.getValsLen()) {
                    Random random = new Random();

                    double maxVal;
                    if (borders[index - 1] < 0) {
                        maxVal = 0.1;
                    }
                    else if (borders[index - 1] < 0.4) {
                        maxVal = 0.5;
                    } else {
                        maxVal = 1;
                    }

                    midBorder = borders[index - 1] + Double.min(0.001,
                            random.nextDouble() * (maxVal - borders[index - 1]));
                    midBorder = Double.min(midBorder, maxVal - 0.001);
//                    if (index == 1) {
//                        midBorder = borders[index - 1] + Double.max(borders[index - 1] / 2, 0.05);
//                    } else {
//                        midBorder = borders[index - 1] + ((borders[index - 1] - border0s[index - 2]) / 2);
//                    }
                } else {
                    midBorder = borders[index - 1] / 2 + ((borders[index] - borders[index - 1]) / 2);
                }

                NewNode newNode = splitNodeHierarchyBySplitter(child.getId(),
                        currentNode.getSplittingIndex(), midBorder);

                currentNode = dbDriver.getNode(currentNodeId); // for gc

                try{
                    rightShift(currentNode.getVals(), index, currentNode.getValsLen());
                } catch (Exception e) {
                    System.out.println(currentNode.isLeaf());
                    e.printStackTrace();
                    throw e;
                }
                currentNode.setValsLen(currentNode.getValsLen() + 1);
                currentNode.getVals()[index] = midBorder;

                rightShift(currentNode.getVectorOrNodesIds(), index, currentNode.getVecOrNodesLen());
                currentNode.setVecOrNodesLen(currentNode.getVecOrNodesLen() + 1);

                if (newNode.left == null) {
                    KDBNode emptyLeaf = KDBNode.emptyLeaf(k,
                            (currentNode.getSplittingIndex() + 1) % vectorSize);
                    currentNode.getVectorOrNodesIds()[index] = dbDriver.saveNode(emptyLeaf);

                } if (newNode.right == null) {
                    KDBNode emptyLeaf = KDBNode.emptyLeaf(k,
                            (currentNode.getSplittingIndex() + 1) % vectorSize);
                    currentNode.getVectorOrNodesIds()[index + 1] = dbDriver.saveNode(emptyLeaf);
                }
                if (newNode.left != null) {
                    if (newNode.left.getValsLen() == 2 * k - 1) {
                        System.out.println("!!! node unexpectedly overflowed !!!");
                        NewNode res = splitNode(newNode.left);
                        KDBNode adapter = generateAdapter(res.splitter,
                                dbDriver.saveNode(res.left),
                                dbDriver.saveNode(res.right),
                                res.left.getSplittingIndex());
                        currentNode.getVectorOrNodesIds()[index] = dbDriver.saveNode(adapter);
                        dbDriver.removeNode(newNode.left);
                    } else {
                        currentNode.getVectorOrNodesIds()[index] = newNode.left.getId();
                    }
                }
                if (newNode.right != null) {
                    if (newNode.right.getValsLen() == 2 * k - 1) {
						System.out.println("!!! node unexpectedly overflowed !!!");
                        NewNode res = splitNode(newNode.right);
                        KDBNode adapter = generateAdapter(res.splitter,
                                dbDriver.saveNode(res.left),
                                dbDriver.saveNode(res.right),
                                res.left.getSplittingIndex());
                        currentNode.getVectorOrNodesIds()[index + 1] = dbDriver.saveNode(adapter);
                        dbDriver.removeNode(newNode.right);
                    } else {
                        currentNode.getVectorOrNodesIds()[index + 1] = newNode.right.getId();
                    }
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
            return vectorId;
        } else {
            InsertResp resp = insert(root.getId(), vector);
            root = dbDriver.getNode(root.getId());
            if (resp.filled) {
                int splittingIndex = normalize((root.getSplittingIndex() - 1) % vectorSize, vectorSize);
                int count = 0;
                while (true) {
                    Random random = new Random();
                     double splitter = random.nextDouble() - 0.5;
                    if (count > 10 * vectorSize) {
                        throw new RuntimeException("infinite loop");
                    }
                    if (count > vectorSize) {
                         splitter = random.nextDouble() * 2 - 1;
                    }
                    NewNode newRoot = splitNodeHierarchyBySplitter(root.getId(),
                            splittingIndex, splitter);

                    if (newRoot.left == null || newRoot.right == null) {
                        splittingIndex--;
                        splittingIndex = normalize(splittingIndex, vectorSize);

                        if (newRoot.left == null) {
                            root = newRoot.right;
                        } else {
                            root = newRoot.left;
                        }
                    } else {

                        double[] vals = new double[2 * k - 1];
                        vals[0] = splitter;
                        long[] vectorIds = new long[2 * k];
                        vectorIds[0] = newRoot.left.getId();
                        vectorIds[1] = newRoot.right.getId();

                        root = KDBNode.builder()
                                .isLeaf(false)
                                .splittingIndex(splittingIndex)
                                .vals(vals)
                                .vectorOrNodesIds(vectorIds)
                                .valsLen(1)
                                .vecOrNodesLen(2)
                                .build();

                        root.setId(dbDriver.saveNode(root));
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

    private List<Long> find(long currentNodeId, double[] baseVector, double[] deltaVector) {
        KDBNode currentNode = dbDriver.getNode(currentNodeId);
        int splittingIndex = currentNode.getSplittingIndex();

        double minVal = baseVector[splittingIndex] - deltaVector[splittingIndex];
        int minIndex = findIndex(currentNode.getVals(), currentNode.getValsLen(), minVal);

        double maxVal = baseVector[splittingIndex] + deltaVector[splittingIndex];
        int maxIndex = findIndex(currentNode.getVals(), currentNode.getValsLen(), maxVal);

        List<Long> ans = new ArrayList<>();
        if (currentNode.isLeaf() && currentNode.getVecOrNodesLen() != 0) {

            for (int i = minIndex; i <= Integer.min(maxIndex, currentNode.getVecOrNodesLen() - 1); i++) {

                DataVector vec = vectorRep.findById(currentNode.getVectorOrNodesIds()[i]);
                boolean fits = true;
                for (int j = 0; j < baseVector.length; j++) {
                    double vecVal = vec.getVector()[j];
                    if (vecVal < baseVector[j] - deltaVector[j] || vecVal > baseVector[j] + deltaVector[j]) {
                        fits = false;
                        break;
                    }
                }
                if (fits) {
                    ans.add(currentNode.getVectorOrNodesIds()[i]);
                }
            }
        } else if (currentNode.getVecOrNodesLen() != 0){
            for (int i = minIndex; i <= maxIndex; i++) {
                ans.addAll(find(currentNode.getVectorOrNodesIds()[i], baseVector, deltaVector));
                currentNode = dbDriver.getNode(currentNodeId); // for gc
            }
        }
        return ans;
    }

    public List<Long> find(double[] baseVector, double[] deltaVector) {
        if (root != null) {
            return find(root.getId(), baseVector, deltaVector);
        }
        return new ArrayList<>(0);
    }

    @AllArgsConstructor
    static class IntLong {
        int i;
        long l;
    }

    @Builder
    static class NewNode {

        double splitter;

        KDBNode left;
        KDBNode right;

    }

    @AllArgsConstructor
    static class InsertResp {

        long vectorId;
        boolean filled;
    }
}
