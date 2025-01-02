package io.github.aloussase.alexandria.application;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        final var response = new HashMap<String, String>();
        response.put("error", ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
