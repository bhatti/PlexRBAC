package com.plexobject.rbac.domain;

public interface Validatable {
    void validate() throws ValidationException;
}
