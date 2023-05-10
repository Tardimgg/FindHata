package com.example.findHataProposalServer.entities.requests;

import com.example.findHataProposalServer.entities.ProposalBD;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeProposalRequest {

    String newTitle;

    String newDescription;

    String newLocation;

    Integer newPrice;

    public void apply(ProposalBD proposalBD) {
        if (newTitle != null) {
            proposalBD.setTitle(newTitle);
        }

        if (newDescription != null) {
            proposalBD.setDescription(newDescription);
        }

        if (newLocation != null) {
            proposalBD.setLocation(newLocation);
        }

        if (newPrice != null) {
            proposalBD.setPrice(newPrice);
        }
    }
}
