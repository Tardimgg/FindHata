package com.example.findHataProposalServer.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Data
@Entity
@Table(name = "reverse_proposal_fact_index")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ReverseProposalFactIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @NonNull
    @ManyToOne
    VectorizedFact vectorizedFact;

    @NotNull
    @NonNull
    @ManyToOne
    ProposalBD proposalBD;

}
