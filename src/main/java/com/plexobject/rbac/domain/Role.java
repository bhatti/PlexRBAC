package com.plexobject.rbac.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
@XmlRootElement
public class Role extends PersistentObject implements Validatable,
        Identifiable<String> {
    public static final Role ANONYMOUS = new Role("anonymous");
    public static final Role DOMAIN_OWNER = new Role("domain_owner");
    public static final Role SUPER_ADMIN = new Role("super_admin");

    @PrimaryKey
    private String id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Role.class)
    String parentRoleID;

    @SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = Subject.class, onRelatedEntityDelete = DeleteAction.NULLIFY)
    Set<String> subjectIDs = new HashSet<String>();

    Role() {
    }

    public Role(String id) {
        setId(id);
    }

    public Role(String id, Role parent) {
        setId(id);
        if (parent != null) {
            setParentRoleID(parent.getId());
        }
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    @XmlTransient
    public Set<String> getSubjectIDs() {
        return Collections.unmodifiableSet(subjectIDs);
    }

    public void setSubjectIDs(Set<String> subjectIDs) {
        firePropertyChange("subjectIDs", this.subjectIDs, subjectIDs);

        this.subjectIDs.clear();
        this.subjectIDs.addAll(subjectIDs);
    }

    public void addSubject(String subjectname) {
        Set<String> old = getSubjectIDs();
        this.subjectIDs.add(subjectname);
        firePropertyChange("subjectIDs", old, this.subjectIDs);

    }

    public void removeSubject(String subjectname) {
        Set<String> old = getSubjectIDs();
        this.subjectIDs.remove(subjectname);
        firePropertyChange("subjectIDs", old, this.subjectIDs);
    }

    public void addSubject(Subject subject) {
        addSubject(subject.getId());
    }

    public void removeSubject(Subject subject) {
        removeSubject(subject.getId());
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
                "subjectIDs", this.subjectIDs).toString();
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
