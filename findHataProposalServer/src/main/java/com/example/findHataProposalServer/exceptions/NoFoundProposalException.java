package com.example.findHataProposalServer.exceptions;

public class NoFoundProposalException extends Exception {

    public NoFoundProposalException(String message) {
        super(message);
    }

    public NoFoundProposalException() {
        super();
    }
}
