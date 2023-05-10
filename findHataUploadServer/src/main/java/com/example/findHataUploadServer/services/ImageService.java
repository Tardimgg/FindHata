package com.example.findHataUploadServer.services;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String saveImage(String id, MultipartFile file);
}
