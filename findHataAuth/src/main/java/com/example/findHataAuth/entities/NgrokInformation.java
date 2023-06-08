package com.example.findHataAuth.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NgrokInformation {

    List<Tunnel> tunnels;

    @Data
    public static class Tunnel {

        @JsonProperty("public_url")
        String publicUrl;

    }
}
