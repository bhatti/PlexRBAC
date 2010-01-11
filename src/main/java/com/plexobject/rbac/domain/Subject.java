package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.utils.PasswordUtils;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class defines a simple subject class Note: This project does not deal
 * with authentication and recommends openid solutions for it (such as RPX)
 * 
 * @author bhatti_shahzad
 * 
 */
@Entity
@XmlRootElement
public class Subject extends PersistentObject implements Validatable,
        Identifiable<String> {
    public static final Subject SUPER_ADMIN = new Subject(Configuration
            .getInstance()
            .getProperty("super_admin_subjectName", "super_admin"),
            PasswordUtils.getHash(Configuration.getInstance().getProperty(
                    "super_admin_credentials", "changeme")));
    @PrimaryKey
    private String id;
    private String credentials;

    // for JPA
    Subject() {
    }

    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (GenericValidator.isBlankOrNull(id)) {
            throw new IllegalArgumentException("subjectName not specified");
        }

        this.id = id;
    }

    public Subject(final String id, final String credentials) {
        setId(id);
        setCredentials(credentials);
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getCredentials() {
        return credentials;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Subject)) {
            return false;
        }
        Subject rhs = (Subject) object;
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
        return new ToStringBuilder(this).append("subjectName", this.id)
                .toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(id)) {
            errorsByField.put("id", "subjectName is not specified");
        }

        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
