package com.example.findHataProposalServer.services.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Service
public class ZkManager {

    @Autowired
    ZooKeeper zooKeeper;

    public void write(String path, String data) {
        try {
            zooKeeper.create(path, data.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String read(String path) {
        try {
            byte[] data = zooKeeper.getData(path, null, null);
            return new String(data, StandardCharsets.UTF_8);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public DistributedLock readLock(String path) {
        return new ReadDistributedLock(zooKeeper, "/" + path);
    }

    public DistributedLock writeLock(String path) {
        return new WriteDistributedLock(zooKeeper, "/" + path);
    }
}
