package com.plexobject.rbac.security;

import java.util.Map;

import com.plexobject.rbac.domain.Permission;

public class SecurityException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final Permission permission;
    private final String username;
    private final String operation;
    private final Map<String, String> userContext;

    public SecurityException(String message, final String username,
            final String operation, final Map<String, String> userContext) {
        super(message);
        this.permission = null;
        this.username = username;
        this.operation = operation;
        this.userContext = userContext;
    }

    public SecurityException(final Permission permission,
            final String username, final String operation,
            final Map<String, String> userContext) {
        this.permission = permission;
        this.username = username;
        this.operation = operation;
        this.userContext = userContext;
    }

    public Permission getPermission() {
        return permission;
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
}
