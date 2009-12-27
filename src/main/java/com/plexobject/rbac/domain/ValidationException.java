package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final Map<String, String> errorsByField = new HashMap<String, String>();

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause,
            Map<String, String> errorsByField) {
        super(message, cause);
        this.errorsByField.putAll(errorsByField);
    }

    public ValidationException(String message, Map<String, String> errorsByField) {
        super(message);
        this.errorsByField.putAll(errorsByField);

    }

    public ValidationException(Throwable cause,
            Map<String, String> errorsByField) {
        super(cause);
        this.errorsByField.putAll(errorsByField);
    }

    public ValidationException(Map<String, String> errorsByField) {
        this.errorsByField.putAll(errorsByField);
    }

    public void addError(final String field, final String message) {
        this.errorsByField.put(field, message);
    }

    public Map<String, String> getValidationErrors() {
        return this.errorsByField;
    }

    @Override
    public String toString() {
        return super.toString() + " " + errorsByField;
    }
}
