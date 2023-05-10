package com.example.findHataProposalServer.entities;

import com.example.findHataProposalServer.entities.requests.AddProposalRequest;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class Proposal {

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
}
