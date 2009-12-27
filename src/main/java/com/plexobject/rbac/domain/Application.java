package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * The application defines a user application that will define a set of
 * permissions and then validates them at runtime
 * 
 */
@Entity
public class Application extends Auditable implements Validatable {
    @PrimaryKey
    private String name;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String ownerUsername;

    // for JPA
    Application() {
    }

    public Application(final String name, final String owner) {
        setName(name);
        setOwnerUsername(owner);
    }

    public void setName(String name) {
        if (GenericValidator.isBlankOrNull(name)) {
            throw new IllegalArgumentException("name is not specified");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOwnerUsername(String ownerUsername) {
        if (GenericValidator.isBlankOrNull(ownerUsername)) {
            throw new IllegalArgumentException("ownerUsername is not specified");
        }
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Application)) {
            return false;
        }
        Application rhs = (Application) object;
        return new EqualsBuilder().append(this.name, rhs.name).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(this.name)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", this.name).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(name)) {
            errorsByField.put("name", "application name is not specified");
        }
        if (GenericValidator.isBlankOrNull(ownerUsername)) {
            errorsByField.put("ownerUsername", "owner user is not specified");
        }
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
