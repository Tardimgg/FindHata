package com.example.findHataProposalServer.configs;

import akka.actor.ActorSystem;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan("com.example.externalRequestFilter.security")
public class Config {

    @Bean
    ActorSystem system() {
        return ActorSystem.create("system");
    }

//    String host = "global-zookeeper";
    String host = "127.0.0.1:2181";
    CountDownLatch connectionLatch = new CountDownLatch(1);

    @Bean
    ZooKeeper zooKeeper() {

        try {
            return new ZooKeeper(host, 20000, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
                System.out.println(watchedEvent.getState());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
