package com.example.findHataAuth.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Endpoint {
    String url;
    String typeCommunication;
}
