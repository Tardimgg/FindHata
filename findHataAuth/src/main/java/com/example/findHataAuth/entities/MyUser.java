package com.example.findHataAuth.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class MyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "login")
    private String login;

    @NonNull
    @Column(name = "hashed_password")
    private String hashedPassword;

    @NonNull
    @Column(name = "roles")
    private List<Role> roles;

    @NonNull
    @Column(name = "hasAlternativeConnection")
    private boolean hasAlternativeConnection = false;


}