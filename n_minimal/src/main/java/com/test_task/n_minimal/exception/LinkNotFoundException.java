package com.test_task.n_minimal.exception;

public class LinkNotFoundException extends RuntimeException{
    public LinkNotFoundException(String message) {
        super(message);
    }
    public LinkNotFoundException() {
        super("Link was not found");
    }
}
