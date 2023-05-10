package com.example.findHataProposalServer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class InitData {
    List<InitProposal> proposals;

    @Data
    @NoArgsConstructor
    public static class InitProposal {

        Integer ownerId;

        String title;

        String description;

        String location;

        Integer price;

        List<String> images;
    }


}

