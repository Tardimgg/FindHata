package com.example.findHataProposalServer.entities;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class ShortInfoProposal {

    @NonNull
    Integer id;

    @NonNull
    String title;

    @NonNull
    Integer price;

    @NonNull
    String location;

    @NonNull
    List<String> images;
}
