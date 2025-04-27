package com.example.findHataProposalServer.services.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class WriteDistributedLock extends DistributedLock {

    WriteDistributedLock(ZooKeeper zooKeeper, String path) {
        super(zooKeeper, path);
    }

    private final String lockPrefix = "/write-";
    private final int writeSuffixLen = lockPrefix.length();

    private final byte[] data = new byte[]{0};
    private String allocatedPath;

    @Override
    public DistributedLock lock() {
        try {
            allocatedPath = zooKeeper.create(path + lockPrefix, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            int allocatedPathNum = toIntWithoutPrefix(allocatedPath, path.length() + writeSuffixLen);

            while (true) {
                List<String> children = zooKeeper.getChildren(path, false);
                int minNum = children.stream()
                        .map((v) -> v.substring(v.lastIndexOf('-') + 1))
                        .mapToInt(Integer::parseInt)
                        .min()
                        .getAsInt();

                if (allocatedPathNum == minNum) {
                    return this;
                }

                Stat stat = zooKeeper.exists(path + lockPrefix + (allocatedPathNum - 1), watchedEvent -> {
                    synchronized (WriteDistributedLock.this) {
                        this.notify();
                    }
                });
                if (stat != null) {
                    synchronized (this) {
                        this.wait();
                    }
                }
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    String getLockPath() {
        if (allocatedPath != null) {
            return allocatedPath;
        }
        throw new RuntimeException("not locked");
    }

    int toIntWithoutPrefix(String data, int prefixLen) {
        return Integer.parseInt(data.substring(prefixLen));
    }


//    @Override
//    void write() {
//
//    }
//
}
