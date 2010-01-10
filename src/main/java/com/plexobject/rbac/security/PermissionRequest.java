package com.plexobject.rbac.security;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PermissionRequest {
    private final String domain;
    private final String subjectname;
    private final String operation;
    private final String target;
    private final Map<String, String> subjectContext;

    public PermissionRequest(final String domain, final String subjectname,
            final String operation, final String target,
            final Map<String, String> subjectContext) {
        this.domain = domain;
        this.subjectname = subjectname;
        this.operation = operation;
        this.target = target;
        this.subjectContext = subjectContext;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * @return the subjectname
     */
    public String getSubjectname() {
        return subjectname;
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
    public Map<String, String> getSubjectContext() {
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
                this.subjectname, rhs.subjectname).append(this.operation,
                rhs.operation).append(this.target, rhs.target).append(
                this.subjectContext, rhs.subjectContext).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(domain)
                .append(subjectname).append(operation).append(target).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(domain).append(subjectname)
                .append(operation).append(target).append(subjectContext)
                .toString();
    }

}
