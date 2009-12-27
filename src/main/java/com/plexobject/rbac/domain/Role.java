package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

public class Role extends Auditable implements Validatable {
    @PrimaryKey(sequence = "role_seq")
    private int id;
    private String rolename;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Application.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String applicationName;

    private Application application;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Role)) {
            return false;
        }
        Role rhs = (Role) object;
        return new EqualsBuilder().append(this.application, rhs.application)
                .append(this.rolename, rhs.rolename).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(this.rolename)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("rolename", this.rolename)
                .toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(rolename)) {
            errorsByField.put("rolename", "rolename is not specified");
        }

        if (applicationName == null) {
            errorsByField
                    .put("applicationName", "applicationName is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }
}
