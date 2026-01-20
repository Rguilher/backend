package br.com.studiogui.backend.controller.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ExceptionDTO(
        String message,
        String statusCode,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,

        String path
) {
    public ExceptionDTO(String message, String statusCode, String path) {
        this(message, statusCode, LocalDateTime.now(), path);
    }
}