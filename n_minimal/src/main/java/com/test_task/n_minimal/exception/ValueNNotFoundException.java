package com.test_task.n_minimal.exception;

public class ValueNNotFoundException extends RuntimeException{
    public ValueNNotFoundException(String message) {
        super(message);
    }
    public ValueNNotFoundException() {
        super("Value N was not found");
    }
}
