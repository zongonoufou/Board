package com.example.Board.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

/** Corps JSON homogene renvoye pour toutes les erreurs de l'API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> fieldErrors;

    private ApiError(HttpStatus status, String message, String path,
                     Map<String, String> fieldErrors) {
        this.timestamp = Instant.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(status, message, path, null);
    }

    public static ApiError of(HttpStatus status, String message, String path,
                              Map<String, String> fieldErrors) {
        return new ApiError(status, message, path, fieldErrors);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
