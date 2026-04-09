package com.enterprise.shellapi.exception;

import com.enterprise.shellapi.dto.ErrorResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {

    private final List<ErrorResponse.FieldError> fieldErrors;

    public ValidationException(List<ErrorResponse.FieldError> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }
}
