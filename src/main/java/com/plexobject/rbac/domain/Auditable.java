package com.plexobject.rbac.domain;

import java.util.Date;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent
public abstract class Auditable {
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Date createdAt = new Date();
    private String createdBy;
    private String createdIPAddress;
    private Date updatedAt = new Date();
    private String updatedBy;
    private String updatedIPAddress;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt not specified");
        }
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("createdBy not specified");
        }
        this.createdBy = createdBy;
    }

    public String getCreatedIPAddress() {
        return createdIPAddress;
    }

    public void setCreatedIPAddress(String createdIPAddress) {
        if (createdIPAddress == null) {
            throw new IllegalArgumentException("createdIPAddress not specified");
        }
        this.createdIPAddress = createdIPAddress;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt not specified");
        }
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        if (updatedBy == null) {
            throw new IllegalArgumentException("updatedBy not specified");
        }
        this.updatedBy = updatedBy;
    }

    public String getUpdatedIPAddress() {
        return updatedIPAddress;
    }

    public void setUpdatedIPAddress(String updatedIPAddress) {
        if (updatedIPAddress == null) {
            throw new IllegalArgumentException("updatedIPAddress not specified");
        }
        this.updatedIPAddress = updatedIPAddress;
    }
}
