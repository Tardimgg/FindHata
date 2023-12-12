package com.example.findHataProposalServer.algorithms.kdb;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DataVector {

    private long id;

    @NonNull
    private double[] vector;
}
