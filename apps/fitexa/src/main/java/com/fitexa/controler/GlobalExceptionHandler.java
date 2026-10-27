package com.fitexa.controler;

import com.fitexa.dtos.ErrorResponseDto;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.InvalidCredentialsException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.exeptions.TenantSubscriptionRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseDto.builder().message(ex.getMessage()).detail("RESOURCE_NOT_FOUND").build());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleBusiness(BusinessValidationException ex) {
        return ResponseEntity.badRequest()
            .body(ErrorResponseDto.builder().message(ex.getMessage()).detail("BUSINESS_VALIDATION").build());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponseDto.builder().message(ex.getMessage()).detail("INVALID_CREDENTIALS").build());
    }

    @ExceptionHandler(TenantSubscriptionRequiredException.class)
    public ResponseEntity<ErrorResponseDto> handleTenantSubscriptionRequired(TenantSubscriptionRequiredException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
            .body(ErrorResponseDto.builder().message(ex.getMessage()).detail("TENANT_SUBSCRIPTION_REQUIRED").build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("Request invalido");
        return ResponseEntity.badRequest()
            .body(ErrorResponseDto.builder().message(message).detail("DTO_VALIDATION").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponseDto.builder().message("Error interno del servidor").detail(ex.getMessage()).build());
    }
}
