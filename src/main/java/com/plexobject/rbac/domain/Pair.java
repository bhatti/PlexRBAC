package com.plexobject.rbac.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Pair<FIRST, SECOND> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public FIRST first;
    public SECOND second;

    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    public FIRST getFirst() {
        return first;
    }

    public void setFirst(FIRST first) {
        this.first = first;
    }

    public SECOND getSecond() {
        return second;
    }

    public void setSecond(SECOND second) {
        this.second = second;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Pair)) {
            return false;
        }
        Pair<FIRST, SECOND> rhs = (Pair<FIRST, SECOND>) object;

        EqualsBuilder eqBuilder = new EqualsBuilder();
        eqBuilder.append(first, rhs.first);
        eqBuilder.append(second, rhs.second);

        return eqBuilder.isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(first).append(
                second).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(first).append(second)
                .toString();
    }
}
