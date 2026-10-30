package com.aquaai.shared.domain;

public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException(String message) {
        super(message, "UNAUTHORIZED_OPERATION");
    }
}
