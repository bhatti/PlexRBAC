package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
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
    public static final Role SUPER_ADMIN = new Role("super_admin");

    @PrimaryKey
    private String id;

    @SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = Role.class)
    Set<String> parentIds = new HashSet<String>();

    @SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = Subject.class, onRelatedEntityDelete = DeleteAction.NULLIFY)
    Set<String> subjectIds = new HashSet<String>();

    Role() {
    }

    public Role(String id) {
        setId(id);
    }

    public Role(String id, Role parent) {
        setId(id);
        if (parent != null) {
            addParentId(parent.getId());
        }
    }

    public Role(String id, Set<String> parentIds) {
        setId(id);
        if (parentIds != null) {
            setParentIds(parentIds);
        }
    }

    @XmlElement
    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    @XmlTransient
    public Set<String> getSubjectIds() {
        return new HashSet<String>(subjectIds);
    }

    public void setSubjectIds(Set<String> subjectIds) {
        firePropertyChange("subjectIds", this.subjectIds, subjectIds);

        this.subjectIds.clear();
        this.subjectIds.addAll(subjectIds);
    }

    public void addSubject(String subjectName) {
        Set<String> old = getSubjectIds();
        this.subjectIds.add(subjectName);
        firePropertyChange("subjectIds", old, this.subjectIds);

    }

    public void removeSubject(String subjectName) {
        Set<String> old = getSubjectIds();
        this.subjectIds.remove(subjectName);
        firePropertyChange("subjectIds", old, this.subjectIds);
    }

    public void addSubject(Subject subject) {
        addSubject(subject.getId());
    }

    public void removeSubject(Subject subject) {
        removeSubject(subject.getId());
    }

    @XmlElement
    public Set<String> getParentIds() {
        return new HashSet<String>(parentIds);
    }

    public boolean hasParentIds() {
        return parentIds != null && parentIds.size() > 0;
    }

    public void setParentIds(Set<String> parentIds) {
        firePropertyChange("parentIds", this.parentIds, parentIds);

        this.parentIds.clear();
        this.parentIds.addAll(parentIds);
    }

    public void addParentId(String parentId) {
        Set<String> old = getParentIds();
        this.parentIds.add(parentId);
        firePropertyChange("parentIds", old, this.parentIds);

    }

    public void removeParentId(String parentId) {
        Set<String> old = getParentIds();
        this.parentIds.remove(parentId);
        firePropertyChange("parentIds", old, this.parentIds);
    }

    public void addParentId(Role parent) {
        addParentId(parent.getId());
    }

    public void removeParentId(Role parent) {
        removeParentId(parent.getId());
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
                "subjectIds", this.subjectIds).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (GenericValidator.isBlankOrNull(id)) {
            errorsByField.put("id", "rolename is not specified");
        }
        if ((parentIds == null || parentIds.size() == 0)
                && !ANONYMOUS.getId().equals(id)) {
            addParentId(ANONYMOUS.getId());
        }
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
