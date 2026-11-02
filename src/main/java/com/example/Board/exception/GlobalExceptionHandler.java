package com.example.Board.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les exceptions metier en reponses HTTP propres au lieu de
 * laisser les RuntimeException remonter en erreur 500 generique.
 *
 * Point important : les exceptions de Spring MVC (ressource introuvable,
 * methode non autorisee, parametre manquant, media type...) implementent
 * {@link ErrorResponse} et portent DEJA leur code HTTP. Le gestionnaire
 * generique doit le respecter, sinon un simple 404 devient un 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex,
                                                    HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Contrainte de donnees non respectee", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation de la requete echouee", request, errors);
    }

    @ExceptionHandler({IllegalArgumentException.class,
                       ConstraintViolationException.class,
                       HttpMessageNotReadableException.class,
                       MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Requete invalide : " + ex.getMessage(), request, null);
    }

    /**
     * Dernier recours. On ne force PAS un 500 : si l'exception vient de la
     * couche web (NoResourceFoundException -> 404, methode non autorisee
     * -> 405, parametre manquant -> 400, ...), on renvoie son statut reel.
     * Seules les vraies erreurs inattendues deviennent un 500, et elles
     * sont journalisees cote serveur avec la stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
            if (status != null) {
                return build(status, springMessage(errorResponse, status), request, null);
            }
        }
        log.error("Erreur inattendue sur {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                     "Erreur interne du serveur", request, null);
    }

    private static String springMessage(ErrorResponse errorResponse, HttpStatus status) {
        ProblemDetail body = errorResponse.getBody();
        if (body != null) {
            if (body.getDetail() != null) {
                return body.getDetail();
            }
            if (body.getTitle() != null) {
                return body.getTitle();
            }
        }
        return status.getReasonPhrase();
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request,
                                           Map<String, String> fieldErrors) {
        ApiError body = (fieldErrors == null)
                ? ApiError.of(status, message, request.getRequestURI())
                : ApiError.of(status, message, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
