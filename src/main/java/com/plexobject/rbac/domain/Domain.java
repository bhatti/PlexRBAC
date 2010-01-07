package com.plexobject.rbac.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;

import com.plexobject.rbac.Configuration;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * The application defines a user application that will define a set of
 * permissions and then validates them at runtime
 * 
 */
@Entity
public class Domain extends PersistentObject implements Validatable,
        Identifiable<String> {
    public static final String DEFAULT_DOMAIN_NAME = Configuration
            .getInstance().getProperty("default.domain", "PlexRBAC");

    @PrimaryKey
    private String id;
    private String description;
    // @SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity =
    // User.class, onRelatedEntityDelete = DeleteAction.NULLIFY)
    Set<String> ownerUsernames = new HashSet<String>();

    // for JPA
    Domain() {
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Domain(final String id, final String description) {
        setID(id);
        setDescription(description);
    }

    void setID(final String id) {
        if (GenericValidator.isBlankOrNull(id)) {
            throw new IllegalArgumentException("id is not specified");
        }
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public Set<String> getOwnerUsernames() {
        return Collections.unmodifiableSet(ownerUsernames);
    }

    public void setOwnerUsernames(final Set<String> ownerUsernames) {
        firePropertyChange("ownerUsernames", this.ownerUsernames,
                ownerUsernames);

        this.ownerUsernames.clear();
        this.ownerUsernames.addAll(ownerUsernames);
    }

    public void addOwner(final String username) {
        if (GenericValidator.isBlankOrNull(username)) {
            throw new IllegalArgumentException("username is not specified");
        }
        Set<String> old = getOwnerUsernames();
        this.ownerUsernames.add(username);
        firePropertyChange("ownerUsernames", old, this.ownerUsernames);

    }

    public void addOwner(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is not specified");
        }
        addOwner(user.getID());
    }

    public void removeOwner(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is not specified");
        }
        removeOwner(user.getID());
    }

    public void removeOwner(final String username) {
        if (GenericValidator.isBlankOrNull(username)) {
            throw new IllegalArgumentException("username is not specified");
        }
        Set<String> old = getOwnerUsernames();
        this.ownerUsernames.remove(username);
        firePropertyChange("ownerUsernames", old, this.ownerUsernames);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Domain)) {
            return false;
        }
        Domain rhs = (Domain) object;
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
        return new ToStringBuilder(this).append("name", this.id).append(
                "owners", this.ownerUsernames).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(id)) {
            errorsByField.put("name", "application name is not specified");
        }
        if (ownerUsernames == null || ownerUsernames.size() == 0) {
            errorsByField.put("ownerUsernames",
                    "domain does not have any owners");
        }
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
