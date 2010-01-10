package com.plexobject.rbac.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent
public abstract class PersistentObject extends BaseDomain {
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Date createdAt = new Date();
    private String createdBy;
    private String createdIPAddress;
    private Date updatedAt = new Date();
    private String updatedBy;
    private String updatedIPAddress;

    @XmlTransient
    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public void setCreatedAt(Date createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt not specified");
        }
        firePropertyChange("createdAt", this.createdAt, createdAt);

        this.createdAt = new Date(createdAt.getTime());
    }

    @XmlTransient
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("createdBy not specified");
        }
        firePropertyChange("createdBy", this.createdBy, createdBy);

        this.createdBy = createdBy;
    }

    public String getCreatedIPAddress() {
        return createdIPAddress;
    }

    public void setCreatedIPAddress(String createdIPAddress) {
        if (createdIPAddress != this.createdIPAddress) {
            firePropertyChange("createdIPAddress", this.createdIPAddress,
                    createdIPAddress);

            this.createdIPAddress = createdIPAddress;
        }
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public void setUpdatedAt(Date updatedAt) {
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt not specified");
        }
        firePropertyChange("updatedAt", this.updatedAt, updatedAt);

        this.updatedAt = new Date(updatedAt.getTime());
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        if (updatedBy == null) {
            throw new IllegalArgumentException("updatedBy not specified");
        }
        firePropertyChange("updatedBy", this.updatedBy, updatedBy);

        this.updatedBy = updatedBy;
    }

    public String getUpdatedIPAddress() {
        return updatedIPAddress;
    }

    public void setUpdatedIPAddress(String updatedIPAddress) {
        if (updatedIPAddress != this.updatedIPAddress) {
            firePropertyChange("updatedIPAddress", this.updatedIPAddress,
                    updatedIPAddress);

            this.updatedIPAddress = updatedIPAddress;
        }
    }
}
