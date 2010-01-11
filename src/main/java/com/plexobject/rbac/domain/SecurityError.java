package com.plexobject.rbac.domain;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class SecurityError extends PersistentObject implements Validatable,
        Identifiable<Integer> {
    @PrimaryKey(sequence = "application_seq")
    private Integer id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String subjectName;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String operation;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String target;
    private Map<String, String> subjectContext;

    // for JPA
    SecurityError() {
    }

    public SecurityError(final String subjectName, final String operation,
            final String target, final Map<String, String> subjectContext) {
        setSubjectName(subjectName);
        setOperation(operation);
        setTarget(target);
        setSubjectContext(subjectContext);
    }

    void setId(Integer id) {
        this.id = id;
    }

    @XmlElement
    public Integer getId() {
        return id;
    }

    public Map<String, String> getSubjectContext() {
        return subjectContext;
    }

    void setSubjectContext(final Map<String, String> subjectContext) {
        if (subjectContext == null) {
            throw new IllegalArgumentException("subject context not specified");
        }
        this.subjectContext = subjectContext;
    }

    void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("subjectName",
                subjectName).append("context", subjectContext).toString();
    }

    @Override
    public void validate() throws ValidationException {
        final Map<String, String> errorsByField = new HashMap<String, String>();
        if (errorsByField.size() > 0) {
            throw new ValidationException(errorsByField);
        }
    }

}
