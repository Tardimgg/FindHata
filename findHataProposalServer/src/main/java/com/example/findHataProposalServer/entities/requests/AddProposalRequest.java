package com.example.findHataProposalServer.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AddProposalRequest {

    @NotNull
    String title;

    @NotNull
    String description;

    @NotNull
    List<String> images;

    @NotNull
    String location;

    @NotNull
    Integer price;

}
