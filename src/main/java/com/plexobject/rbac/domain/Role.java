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

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Role extends Auditable implements Validatable,
        Identifiable<String> {
    public static final Role ANONYMOUS = new Role("anonymous");
    @PrimaryKey
    private String id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Role.class)
    String parentRoleID;

    @SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = User.class, onRelatedEntityDelete = DeleteAction.NULLIFY)
    Set<String> userIDs = new HashSet<String>();

    Role() {
    }

    public Role(String id) {
        setID(id);
    }

    public Role(String id, Role parent) {
        setID(id);
        if (parent != null) {
            setParentRoleID(parent.getID());
        }
    }

    public String getID() {
        return id;
    }

    void setID(String id) {
        this.id = id;
    }

    public Set<String> getUserIDs() {
        return Collections.unmodifiableSet(userIDs);
    }

    public void setUserIDs(Set<String> userIDs) {
        firePropertyChange("userIDs", this.userIDs, userIDs);

        this.userIDs.clear();
        this.userIDs.addAll(userIDs);
    }

    public void addUser(User user) {
        Set<String> old = getUserIDs();
        this.userIDs.add(user.getID());
        firePropertyChange("userIDs", old, this.userIDs);
    }

    public void removeUser(User user) {
        Set<String> old = getUserIDs();
        this.userIDs.remove(user.getID());
        firePropertyChange("userIDs", old, this.userIDs);
    }

    public String getParentRoleID() {
        return parentRoleID;
    }

    public boolean hasParentRoleID() {
        return !GenericValidator.isBlankOrNull(parentRoleID);
    }

    public void setParentRoleID(String parentRoleID) {
        firePropertyChange("parentRoleID", this.parentRoleID, parentRoleID);

        this.parentRoleID = parentRoleID;
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
        return new ToStringBuilder(this).append("rolename", this.id).append(
                "userIDs", this.userIDs).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(id)) {
            errorsByField.put("id", "rolename is not specified");
        }
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }
}
