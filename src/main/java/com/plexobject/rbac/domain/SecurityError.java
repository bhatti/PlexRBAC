package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class SecurityError extends Auditable implements Validatable,
        Identifiable<Integer> {
    @PrimaryKey(sequence = "application_seq")
    private Integer id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Permission.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private Integer permissionId;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = User.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String username;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String operation;
    private Map<String, String> userContext;

    // for JPA
    SecurityError() {
    }

    public SecurityError(final Integer permissionId,
            final Map<String, String> userContext) {
        setPermissionId(permissionId);
        setUserContext(userContext);
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public Integer getID() {
        return id;
    }

    public Map<String, String> getUserContext() {
        return userContext;
    }

    public void setUserContext(final Map<String, String> userContext) {
        if (userContext == null) {
            throw new IllegalArgumentException("user context not specified");
        }
        this.userContext = userContext;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    // for JPA
    void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    Integer getPermissionId() {
        return permissionId;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("username",
                username).append("permissionId", this.permissionId).append(
                "context", userContext).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (permissionId == null) {
            errorsByField.put("permissionId", "permissionId is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
