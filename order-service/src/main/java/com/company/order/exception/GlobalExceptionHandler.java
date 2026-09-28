package com.company.order.exception;

import com.company.order.InventoryReservationException;
import com.company.order.UserNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // =========================
    // USER NOT FOUND
    // =========================

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(
            UserNotFoundException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("error", ex.getMessage());

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND
        );
    }


    // =========================
    // INSUFFICIENT INVENTORY
    // =========================

    @ExceptionHandler(InventoryReservationException.class)
    public ResponseEntity<Map<String, String>> handleInventoryReservation(
            InventoryReservationException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("error", ex.getMessage());

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST
        );
    }


    // =========================
    // VALIDATION ERRORS
    // =========================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach((error) -> {

                    String fieldName =
                            ((FieldError) error).getField();

                    String errorMessage =
                            error.getDefaultMessage();

                    errors.put(fieldName, errorMessage);
                });

        return new ResponseEntity<>(
                errors,
                HttpStatus.BAD_REQUEST
        );
    }


    // =========================
    // OTHER RUNTIME ERRORS
    // =========================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("error", ex.getMessage());

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}