package com.plexobject.rbac.eval.simple;

import java.util.regex.Pattern;

import org.apache.commons.validator.GenericValidator;

public enum Type {
    STRING, NUMBER, TIME;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[\\d\\.]+");

    public static boolean isNumber(final String value) {
        if (GenericValidator.isBlankOrNull(value)) {
            return false;
        }
        return NUMBER_PATTERN.matcher(value).matches();
    }

    public static double number(final String value) {
        return new Double(value).doubleValue();
    }
}