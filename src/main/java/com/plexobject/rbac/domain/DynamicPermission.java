package com.plexobject.rbac.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class DynamicPermission extends Permission {
    @SecondaryKey(relate = Relationship.ONE_TO_MANY)
    private Collection<ContextProperty> context = new HashSet<ContextProperty>();

    {
        setEntityType("DynamicPermission");
    }

    // for JPA
    DynamicPermission() {
    }

    public DynamicPermission(final Application application,
            final String operation, final String target,
            final Collection<ContextProperty> context) {
        super(application, operation, target);
        setContext(context);
    }

    /**
     * The context is runtime value that is checked
     * 
     * @return
     */
    public Collection<ContextProperty> getContext() {
        return context;
    }

    public void setContext(Collection<ContextProperty> context) {
        if (context == null) {
            throw new IllegalArgumentException("Context is not specified");
        }
        this.context = context;
    }

    public boolean impliesContext(final String... keyValues) {
        Map<String, String> userContext = new HashMap<String, String>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            userContext.put(keyValues[i], keyValues[i + 1]);
        }
        return impliesContext(userContext);
    }

    /**
     * This context verifies if the user context includes the context defined in
     * the permission
     * 
     * @param userContext
     * @return
     */
    public boolean impliesContext(final Map<String, String> userContext) {
        if (userContext == context) {
            return true;
        }
        if (userContext == null) {
            return false;
        }
        for (ContextProperty cp : context) {
            final String userValue = userContext.get(cp.getName());
            if (userValue == null) {
                return false;
            }
            if (!cp.implies(userValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DynamicPermission)) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        DynamicPermission rhs = (DynamicPermission) object;
        return new EqualsBuilder().append(this.context, rhs.context).isEquals();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString()
                + new ToStringBuilder(this).append("context", this.context)
                        .toString();
    }
}
