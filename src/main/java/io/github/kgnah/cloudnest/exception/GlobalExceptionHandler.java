package io.github.kgnah.cloudnest.exception;

import io.github.kgnah.cloudnest.dto.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // validation exception handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
        MethodArgumentNotValidException ex
    ){
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(400, "Validation failed", errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // response status exception handler
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getStatusCode().value(),
                ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    // deployment not found exception handler
    @ExceptionHandler(DeploymentNotFoundException.class)
    public ResponseEntity<?> handDeploymentNotFoundException(DeploymentNotFoundException ex) {

        // error response
        ErrorResponse errorResponse = new ErrorResponse(404, ex.getMessage());

        return ResponseEntity.status(404).body(errorResponse);
    }
}
