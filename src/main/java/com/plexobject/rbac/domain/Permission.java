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

/**
 * This class represents a permission in the system that defines three
 * dimensions - subject - operation - target
 * 
 */
@Entity
@XmlRootElement
public class Permission extends PersistentObject implements Validatable,
        Identifiable<Integer> {

    @PrimaryKey(sequence = "permission_seq")
    private Integer id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String operation; // can be string or regular expression
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String target;
    private String expression;

    @SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = Role.class, onRelatedEntityDelete = DeleteAction.NULLIFY)
    Set<String> roleIds = new HashSet<String>();

    // for JPA
    Permission() {
    }

    public Permission(final String operation, final String target,
            final String expression) {
        setOperation(operation);
        setTarget(target);
        setExpression(expression);
    }

    void setId(Integer id) {
        this.id = id;
    }

    @XmlElement
    public Integer getId() {
        return id;
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
    public boolean implies(final String op, final String tgt) {
        if (GenericValidator.isBlankOrNull(op)) {
            return false;
        }
        if (GenericValidator.isBlankOrNull(tgt)) {
            return false;
        }

        return (operation.equalsIgnoreCase(op) || op.toLowerCase().matches(
                operation))
                && (target.equalsIgnoreCase(tgt) || tgt.toLowerCase().matches(
                        target));
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
        firePropertyChange("operation", this.operation, operation);

        this.operation = operation.toLowerCase();
    }

    public void setTarget(String target) {
        if (GenericValidator.isBlankOrNull(target)) {
            throw new IllegalArgumentException("target not specified");
        }
        firePropertyChange("target", this.target, target);

        this.target = target;
    }

    /**
     * 
     * @return
     */
    public String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        firePropertyChange("expression", this.expression, expression);

        this.expression = expression;
    }

    @XmlTransient
    public Set<String> getRoleIds() {
        return new HashSet<String>(roleIds);
    }

    public void setRoleIds(Set<String> roleIds) {
        firePropertyChange("roleIds", this.roleIds, roleIds);

        this.roleIds.clear();
        this.roleIds.addAll(roleIds);
    }

    public void addRole(Role role) {
        Set<String> old = getRoleIds();
        this.roleIds.add(role.getId());
        firePropertyChange("roleIds", old, this.roleIds);
    }

    public void removeRole(Role role) {
        Set<String> old = getRoleIds();
        this.roleIds.remove(role.getId());
        firePropertyChange("roleIds", old, this.roleIds);
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
        return new EqualsBuilder().append(this.operation, rhs.operation)
                .append(this.target, rhs.target).append(this.expression,
                        rhs.expression).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713)
                .append(this.operation).append(this.target).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append(
                "operation", this.operation).append("target", target).append(
                "expression", this.expression).append("roleIds", this.roleIds)
                .toString();
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
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }
}
