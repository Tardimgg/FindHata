package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.entities.requests.AddProposalRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String saveImage(String id, MultipartFile file);
}
