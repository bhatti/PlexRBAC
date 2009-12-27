package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * This class represents a permission in the system that defines three
 * dimensions - subject - operation - target
 * 
 */
@Entity
public class Permission extends Auditable implements Validatable {
    @PrimaryKey(sequence = "permission_seq")
    private int id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String operation; // can be string or regular expression
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String target;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Application.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String applicationName;

    private Application application;
    private String entityType = "Permission";

    // for JPA
    Permission() {
    }

    public Permission(final Application application, final String operation,
            final String target) {
        setApplication(application);
        setOperation(operation);
        setTarget(target);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * The application that defines this permission
     * 
     * @return
     */
    public Application getApplication() {
        return application;
    }

    /**
     * The operation is action like read/write/update/delete or regular
     * expression
     * 
     * @return
     */
    public String getOperation() {
        return operation;
    }

    /**
     * This method matches operation by equality or regular expression
     * 
     * @param action
     * @return
     */
    public boolean impliesOperation(final String op) {
        if (GenericValidator.isBlankOrNull(op)) {
            return false;
        }
        return operation.equalsIgnoreCase(op)
                || op.toLowerCase().matches(operation);
    }

    /**
     * The target is object that is being acted upon such as file, row in the
     * database
     * 
     * @return
     */
    public String getTarget() {
        return target;
    }

    public void setOperation(String operation) {
        if (GenericValidator.isBlankOrNull(operation)) {
            throw new IllegalArgumentException("operation not specified");
        }
        if (operation.equals("*")) {
            operation = ".*";
        }
        this.operation = operation.toLowerCase();
    }

    public void setTarget(String target) {
        if (GenericValidator.isBlankOrNull(target)) {
            throw new IllegalArgumentException("target not specified");
        }
        this.target = target;
    }

    public void setApplication(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("application not specified");
        }

        this.application = application;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Permission)) {
            return false;
        }
        Permission rhs = (Permission) object;
        return new EqualsBuilder().append(this.application, rhs.application)
                .append(this.operation, rhs.operation).append(this.target,
                        rhs.target).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(
                this.application).append(this.operation).append(this.target)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append(
                "application", this.application).append("operation",
                this.operation).append("target", target).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(operation)) {
            errorsByField.put("operation", "operation is not specified");
        }
        if (GenericValidator.isBlankOrNull(target)) {
            errorsByField.put("target", "target is not specified");
        }

        if (applicationName == null) {
            errorsByField
                    .put("applicationName", "applicationName is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

    void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    String getEntityType() {
        return entityType;
    }
}
