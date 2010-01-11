package com.plexobject.rbac.security;

import java.util.Map;

public class SecurityException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String subjectName;
    private final String operation;
    private final String target;

    private final Map<String, String> subjectContext;

    public SecurityException(String message, final String subjectName,
            final String operation, final String target,
            final Map<String, String> subjectContext) {
        super(message);
        this.subjectName = subjectName;
        this.operation = operation;
        this.target = target;
        this.subjectContext = subjectContext;
    }

    public SecurityException(final String subjectName, final String operation,
            final String target, final Map<String, String> subjectContext) {
        this.subjectName = subjectName;
        this.operation = operation;
        this.target = target;
        this.subjectContext = subjectContext;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public Map<String, String> getSubjectContext() {
        return subjectContext;
    }

    public String getOperation() {
        return operation;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return super.toString() + ", subjectName " + subjectName + ", operation "
                + operation + ", target " + target + ", context " + subjectContext;
    }
}
