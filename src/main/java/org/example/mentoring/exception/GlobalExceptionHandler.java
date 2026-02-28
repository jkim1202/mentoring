package org.example.mentoring.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e){
        ErrorCode ec = e.getErrorCode();

        return ResponseEntity.status(ec.getStatus())
                .body(Map.of(
                        "status", ec.getStatus().value(),
                        "code", ec.getCode(),
                        "message", ec.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotValidExceptionException(MethodArgumentNotValidException e){
        ErrorCode ec = ErrorCode.COMMON_INVALID_INPUT;
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(ec.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(Map.of(
                        "status", ec.getStatus().value(),
                        "code", ec.getCode(),
                        "message", message
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e){
        ErrorCode ec = ErrorCode.COMMON_INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(ec.getStatus())
                .body(Map.of(
                        "status", ec.getStatus().value(),
                        "code", ec.getCode(),
                        "message", ec.getMessage()
                ));
    }
}
