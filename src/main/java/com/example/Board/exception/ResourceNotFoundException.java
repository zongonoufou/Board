package com.example.Board.exception;

/** Ressource absente : traduite en HTTP 404 par GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
