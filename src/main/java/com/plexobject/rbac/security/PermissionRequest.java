package com.plexobject.rbac.security;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PermissionRequest {
    private final String domain;
    private final String username;
    private final String operation;
    private final String target;
    private final Map<String, String> userContext;

    public PermissionRequest(final String domain, final String username,
            final String operation, final String target,
            final Map<String, String> userContext) {
        this.domain = domain;
        this.username = username;
        this.operation = operation;
        this.target = target;
        this.userContext = userContext;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
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
     * @return the userContext
     */
    public Map<String, String> getUserContext() {
        return userContext;
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
                this.username, rhs.username).append(this.operation,
                rhs.operation).append(this.target, rhs.target).append(
                this.userContext, rhs.userContext).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(domain)
                .append(username).append(operation).append(target).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(domain).append(username)
                .append(operation).append(target).append(userContext)
                .toString();
    }

}
