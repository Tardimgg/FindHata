package com.example.findHataProposalServer.services.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public abstract class DistributedLock {

    protected final String path;
    protected final ZooKeeper zooKeeper;
    protected String lockPath;

    private static final byte[] data = new byte[]{};

    DistributedLock(ZooKeeper zooKeeper, String path) {
        this.zooKeeper = zooKeeper;
        this.path = path;
        try {
            if (zooKeeper.exists(path, false) == null) {
                zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public abstract DistributedLock lock();

    abstract String getLockPath();

    public void unlock() {
        try {
            zooKeeper.delete(getLockPath(), -1);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    void read() {
        throw new RuntimeException("read not allowed");
    }

    void write() {
        throw new RuntimeException("write not allowed");
    }

}
