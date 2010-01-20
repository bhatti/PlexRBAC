package com.plexobject.rbac.security;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PermissionRequest {
    private final String domain;
    private final String subjectName;
    private final String operation;
    private final String target;
    private final Map<String, Object> subjectContext = new TreeMap<String, Object>();

    public PermissionRequest(final String domain, final String subjectName,
            final String operation, final String target,
            final Map<String, Object> subjectContext) {
        this.domain = domain;
        this.subjectName = subjectName;
        this.operation = operation;
        this.target = target;
        if (subjectContext != null) {
            this.subjectContext.putAll(subjectContext);
        }
    }

    public String getDomain() {
        return domain;
    }

    /**
     * @return the subjectName
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return the subjectContext
     */
    public Map<String, Object> getSubjectContext() {
        return subjectContext;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PermissionRequest)) {
            return false;
        }
        PermissionRequest rhs = (PermissionRequest) object;
        return new EqualsBuilder().append(this.domain, domain).append(
                this.subjectName, rhs.subjectName).append(this.operation,
                rhs.operation).append(this.target, rhs.target).append(
                this.subjectContext, rhs.subjectContext).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(domain)
                .append(subjectName).append(operation).append(target)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(domain).append(subjectName)
                .append(operation).append(target).append(subjectContext)
                .toString();
    }

}
