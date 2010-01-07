package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.Configuration;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class defines a simple user class Note: This project does not deal with
 * authentication and recommends openid solutions for it (such as RPX)
 * 
 * @author bhatti_shahzad
 * 
 */
@Entity
public class User extends PersistentObject implements Validatable,
        Identifiable<String> {
    public static final String SUPER_ADMIN_USERNAME = Configuration
            .getInstance().getProperty("super_admin_username",
                    "super_admin_user");
    @PrimaryKey
    private String id;

    // for JPA
    User() {
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        if (GenericValidator.isBlankOrNull(id)) {
            throw new IllegalArgumentException("username not specified");
        }

        this.id = id;
    }

    public User(final String id) {
        setID(id);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User rhs = (User) object;
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
        return new ToStringBuilder(this).append("username", this.id).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(id)) {
            errorsByField.put("id", "username is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
