package com.koriebruh.demoreactivenw.exceptions;

import com.koriebruh.demoreactivenw.dto.ApiResponse;
import com.koriebruh.demoreactivenw.dto.ApiResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptions {

    private final ApiResponseFactory responseFactory;

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleValidation(
            WebExchangeBindException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return Mono.just(
                ResponseEntity.badRequest().body(
                        responseFactory.errors(
                                "Validation failed",
                                errors
                        )
                )
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleRuntime(
            RuntimeException ex
    ) {
        return Mono.just(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        responseFactory.error(ex.getMessage())
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleGeneric(
            Exception ex
    ) {
        return Mono.just(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        responseFactory.error("Internal server error")
                )
        );
    }

}
