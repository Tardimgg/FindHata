package com.example.findHataProposalServer.entities;


import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@Table(name = "image_paths")
@AllArgsConstructor
@NoArgsConstructor
public class ImagePath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "path")
    String path;
}
