package com.example.findHataProposalServer.entities.responses;

import com.example.findHataProposalServer.entities.ImagePath;
import com.example.findHataProposalServer.entities.ProposalBD;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ProposalResponse {

    @NonNull
    Integer proposalId;

    @NonNull
    Integer ownerId;

    @NonNull
    String title;

    @NonNull
    String description;

    @NonNull
    String location;

    @NonNull
    Integer price;

    @NonNull
    List<String> images;

    public static ProposalResponse fromBD(ProposalBD source) {
        return ProposalResponse.builder()
                .proposalId(source.getId())
                .title(source.getTitle())
                .description(source.getDescription())
                .location(source.getLocation())
                .images(source.getImages().stream().map(ImagePath::getPath).collect(Collectors.toList()))
                .price(source.getPrice())
                .ownerId(source.getOwnerId())
                .build();
    }
}
