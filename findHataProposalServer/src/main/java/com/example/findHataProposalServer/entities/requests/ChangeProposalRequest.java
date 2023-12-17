package com.example.findHataProposalServer.entities.requests;

import com.example.findHataProposalServer.entities.ProposalDB;
import lombok.Data;

@Data
public class ChangeProposalRequest {

    String newTitle;

    String newDescription;

    String newLocation;

    Integer newPrice;

    public void apply(ProposalDB proposalDB) {
        if (newTitle != null) {
            proposalDB.setTitle(newTitle);
        }

        if (newDescription != null) {
            proposalDB.setDescription(newDescription);
        }

        if (newLocation != null) {
            proposalDB.setLocation(newLocation);
        }

        if (newPrice != null) {
            proposalDB.setPrice(newPrice);
        }
    }
}
