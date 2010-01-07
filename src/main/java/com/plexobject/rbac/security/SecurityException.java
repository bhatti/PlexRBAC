package com.plexobject.rbac.security;

import java.util.Map;

public class SecurityException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String operation;
    private final String target;

    private final Map<String, String> userContext;

    public SecurityException(String message, final String username,
            final String operation, final String target,
            final Map<String, String> userContext) {
        super(message);
        this.username = username;
        this.operation = operation;
        this.target = target;
        this.userContext = userContext;
    }

    public SecurityException(final String username, final String operation,
            final String target, final Map<String, String> userContext) {
        this.username = username;
        this.operation = operation;
        this.target = target;
        this.userContext = userContext;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, String> getUserContext() {
        return userContext;
    }

    public String getOperation() {
        return operation;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return super.toString() + ", username " + username + ", operation "
                + operation + ", target " + target + ", context " + userContext;
    }
}
