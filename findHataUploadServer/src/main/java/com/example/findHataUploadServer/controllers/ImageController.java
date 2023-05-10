package com.example.findHataUploadServer.controllers;

import com.example.findHataUploadServer.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class ImageController {

    @Autowired
    ImageService imageService;

    @PostMapping("/save-image")
    public Map<String, String> addProposal(@RequestParam("image") MultipartFile image) {

        String path = imageService.saveImage("1", image);

        return Map.of("status", "ok", "path", path);

    }
}
