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
public class Application extends Auditable implements Validatable,
        Identifiable<String> {
    @PrimaryKey
    private String id;
    private String descirption;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String ownerUsername;

    // for JPA
    Application() {
    }

    public Application(final String id, final String owner) {
        setID(id);
        setOwnerUsername(owner);
    }

    public void setID(String id) {
        if (GenericValidator.isBlankOrNull(id)) {
            throw new IllegalArgumentException("id is not specified");
        }
        this.id = id;
    }

    public String getID() {
        return id;
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
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(this.id)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", this.id).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(id)) {
            errorsByField.put("name", "application name is not specified");
        }
        if (GenericValidator.isBlankOrNull(ownerUsername)) {
            errorsByField.put("ownerUsername", "owner user is not specified");
        }
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

    public void setDescirption(String descirption) {
        this.descirption = descirption;
    }

    public String getDescirption() {
        return descirption;
    }

}
