package com.example.findHataNotificationServer.entities.requests;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class EndpointRequest {

    @NonNull
    String url;

    @NonNull
    String typeCommunication;
}
