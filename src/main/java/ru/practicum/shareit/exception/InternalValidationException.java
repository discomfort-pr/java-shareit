package ru.practicum.shareit.exception;

public class InternalValidationException extends RuntimeException {
    public InternalValidationException(String message) {
        super(message);
    }
}
