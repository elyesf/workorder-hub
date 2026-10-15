package dev.elyes.workorders.common;

/** The request is valid but clashes with the current state, for example a forbidden status change. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
