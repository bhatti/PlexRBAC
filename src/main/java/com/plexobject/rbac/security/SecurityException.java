package com.plexobject.rbac.security;

import java.util.Map;

public class SecurityException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String subjectname;
    private final String operation;
    private final String target;

    private final Map<String, String> subjectContext;

    public SecurityException(String message, final String subjectname,
            final String operation, final String target,
            final Map<String, String> subjectContext) {
        super(message);
        this.subjectname = subjectname;
        this.operation = operation;
        this.target = target;
        this.subjectContext = subjectContext;
    }

    public SecurityException(final String subjectname, final String operation,
            final String target, final Map<String, String> subjectContext) {
        this.subjectname = subjectname;
        this.operation = operation;
        this.target = target;
        this.subjectContext = subjectContext;
    }

    public String getSubjectname() {
        return subjectname;
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
        return super.toString() + ", subjectname " + subjectname + ", operation "
                + operation + ", target " + target + ", context " + subjectContext;
    }
}
