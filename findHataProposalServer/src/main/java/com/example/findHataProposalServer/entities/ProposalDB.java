package com.example.findHataProposalServer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@Entity
@Table(name = "proposals")
@AllArgsConstructor
@NoArgsConstructor
public class ProposalDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "owner_id")
    Integer ownerId;

    @NonNull
    @Column(name = "title")
    String title;

    @NonNull
    @Column(columnDefinition = "TEXT", name = "description")
    String description;

    @NonNull
    @Column(name = "location")
    String location;

    @NonNull
    @Column(name = "price")
    Integer price;

    @NonNull
    @OneToMany
    List<ImagePath> images;
}
