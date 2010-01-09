package com.plexobject.rbac.repository;

public class NotFoundException extends PersistenceException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

}
