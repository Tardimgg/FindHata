package com.example.findHataUploadServer.services;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.StandardCopyOption;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${externalUrlWithoutPort}")
    String externalUrl;

    @Value("${uploadServerPort}")
    String port;

    @PostConstruct
    void init() {
        ApplicationHome home = new ApplicationHome(ImageServiceImpl.class);
        File folder = new File(home.getDir().getAbsoluteFile() + "/images");
        folder.mkdir();
    }

    @Override
    @SneakyThrows
    public String saveImage(String userId, MultipartFile file) {
        ApplicationHome home = new ApplicationHome(ImageServiceImpl.class);

        String folderPath = "/images/" + userId + "/" + System.currentTimeMillis();
        File folder = new File(home.getDir().getAbsoluteFile() + folderPath);

        folder.mkdirs();

        File target = new File(folder.getAbsoluteFile() + "/" + file.getOriginalFilename());

        java.nio.file.Files.copy(
                file.getInputStream(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        return externalUrl + ":" + port +  folderPath + "/" + file.getOriginalFilename();
    }
}
