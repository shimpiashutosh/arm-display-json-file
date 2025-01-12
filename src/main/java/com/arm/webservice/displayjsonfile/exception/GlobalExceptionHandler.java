package com.arm.webservice.displayjsonfile.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.arm.webservice.displayjsonfile.constant.OpenAPIConstants.FILE_READ_ERROR_MESSAGE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Global exception handler to capture errors & return appropriate responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * @param e exception to capture.
     *
     * @return 500 response with error message.
     */
    @ExceptionHandler({IOException.class, ResponseStatusException.class})
    public Mono<ResponseEntity<String>> handleResponseStatusException(final Exception e) {
        LOGGER.error("Error processing request: %s".formatted(e.getMessage()), e);
        return Mono.just(ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(FILE_READ_ERROR_MESSAGE));
    }
}
