package com.example.findHataProposalServer.services;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import com.example.findHataProposalServer.VectorizationServiceClient;
import com.example.findHataProposalServer.VectorizationServiceGrpc;
import com.example.findHataProposalServer.VectorizationServiceHandlerFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class VectorizationServiceRep {


    @Value("${vectorized_service_host}")
    private String serviceHost;

    @Value("${vectorized_service_post}")
    private Integer servicePort;

//    ManagedChannel channel;
//    VectorizationServiceGrpc.VectorizationServiceBlockingStub blockingStub;
//    VectorizationServiceGrpc.VectorizationServiceStub asyncStub;

    @Autowired
    private ActorSystem actorSystem;

    VectorizationServiceClient client;


    @PostConstruct
    void init() {
        client = VectorizationServiceClient.create(
//                GrpcClientSettings.fromConfig("profile.VectorizationService", actorSystem),
                GrpcClientSettings.connectToServiceAt(serviceHost, servicePort, actorSystem).withTls(false),
                actorSystem
        );

//        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
//                .forTarget(serviceHost + ":" + servicePort).usePlaintext();
//        channel = channelBuilder.build();
//        blockingStub = VectorizationServiceGrpc.newBlockingStub(channel);
//        asyncStub = VectorizationServiceGrpc.newStub(channel);
    }

}
