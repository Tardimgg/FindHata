package com.example.findHataAuth.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShortInfoUser {
    List<Role> roles;
    Integer id;
    boolean hasAlternativeConnection;

}
