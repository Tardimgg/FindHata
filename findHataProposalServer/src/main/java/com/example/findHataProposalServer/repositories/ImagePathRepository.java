package com.example.findHataProposalServer.repositories;

import com.example.findHataProposalServer.entities.ImagePath;
import com.example.findHataProposalServer.entities.ProposalBD;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagePathRepository extends JpaRepository<ImagePath, Integer> {

    ImagePath save(@NonNull ImagePath imagePath);

}
