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
public class SecurityError extends Auditable implements Validatable {
    @PrimaryKey(sequence = "application_seq")
    private int id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Permission.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private Integer permissionId;
    private Permission permission;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Application.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private Integer applicationName;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = User.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String username;
    private Map<String, String> userContext;

    // for JPA
    SecurityError() {
    }

    public SecurityError(final Permission permission,
            final Map<String, String> userContext) {
        setPermission(permission);
        setUserContext(userContext);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
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

    public void setPermission(final Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission not specified");
        }
        this.permission = permission;
    }

    public Permission getPermission() {
        return permission;
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append(
                "applicationName", applicationName).append("username", username)
                .append("permission", this.permission).append("context",
                        userContext).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();

        if (applicationName == null) {
            errorsByField
                    .put("applicationName", "applicationNameis not specified");
        }
        if (permissionId == null) {
            errorsByField.put("permissionId", "permissionId is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
