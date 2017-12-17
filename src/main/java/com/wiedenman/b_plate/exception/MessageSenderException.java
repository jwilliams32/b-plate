package com.wiedenman.b_plate.exception;

public class MessageSenderException extends DomainException {

    public MessageSenderException(final String message, final Exception e) {
        super(message, e);
    }
}
