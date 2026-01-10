package br.com.studiogui.backend.controller.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionDTO> handle404(EntityNotFoundException exception, HttpServletRequest request) {
        ExceptionDTO response = new ExceptionDTO(
                exception.getMessage(),
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDTO> handleBadRequest(IllegalArgumentException exception, HttpServletRequest request) {
        ExceptionDTO response = new ExceptionDTO(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(Exception exception, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(
                new ExceptionDTO(exception.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        LocalDateTime.now(),
                        request.getRequestURI())
        );
    }
}
