package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.VectorizationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class VectorizationServiceClient {


    @Value("${vectorized_service_host}")
    String serviceHost;

    @Value("${vectorized_service_post}")
    Integer servicePort;

    ManagedChannel channel;
    VectorizationServiceGrpc.VectorizationServiceBlockingStub blockingStub;
    VectorizationServiceGrpc.VectorizationServiceStub asyncStub;

    @PostConstruct
    void init() {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forTarget(serviceHost + ":" + servicePort).usePlaintext();
        channel = channelBuilder.build();
        blockingStub = VectorizationServiceGrpc.newBlockingStub(channel);
        asyncStub = VectorizationServiceGrpc.newStub(channel);
    }

}
