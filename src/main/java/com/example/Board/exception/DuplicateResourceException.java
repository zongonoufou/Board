package com.example.Board.exception;

/** Conflit d'unicite (ex. email deja utilise) : traduit en HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}
