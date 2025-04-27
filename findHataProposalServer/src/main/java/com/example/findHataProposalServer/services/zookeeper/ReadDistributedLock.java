package com.example.findHataProposalServer.services.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class ReadDistributedLock extends DistributedLock {

    private final String lockPrefix;

    ReadDistributedLock(ZooKeeper zooKeeper, String path, String lockPrefix) {
        super(zooKeeper, path);
        this.lockPrefix = lockPrefix;
        writeSuffixLen = lockPrefix.length();
    }

    ReadDistributedLock(ZooKeeper zooKeeper, String path) {
        super(zooKeeper, path);
        lockPrefix = "/read-";
        writeSuffixLen = lockPrefix.length();
    }

    private final int writeSuffixLen;

    private final byte[] data = new byte[]{0};

    String allocatedPath;

    boolean hasLock = false;

    @Override
    public DistributedLock lock() {
        try {
            allocatedPath = zooKeeper.create(path + lockPrefix, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            int allocatedPathNum = toIntWithoutPrefix(allocatedPath, path.length() + writeSuffixLen);

            while (true) {
                List<String> children = zooKeeper.getChildren(path, false);
                int minNum = children.stream()
                        .filter((v) -> v.substring(path.length()).startsWith("write"))
                        .mapToInt((v) -> toIntWithoutPrefix(v, path.length() + writeSuffixLen))
                        .min()
                        .orElse(-1);

                if (minNum == -1 || minNum > allocatedPathNum) {
                    hasLock = true;
                    return this;
                }

                Stat stat = zooKeeper.exists(path + lockPrefix + (allocatedPathNum - 1), watchedEvent -> {
                    synchronized (ReadDistributedLock.this) {
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

    int toIntWithoutPrefix(String data, int prefixLen) {
        return Integer.parseInt(data.substring(prefixLen));
    }

    @Override
    String getLockPath() {
        if (allocatedPath != null) {
            return allocatedPath;
        }
        throw new RuntimeException("not locked");
    }


//    @Override
//    void read() {
//
//    }
}
