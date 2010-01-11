package com.plexobject.rbac.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.time.DateUtils;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent
public abstract class PersistentObject extends BaseDomain {
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Date createdAt = new Date();
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private Date deletedAt = DateUtils.addYears(new Date(), 50); // by -default
                                                                 // all objects
                                                                 // expires in
                                                                 // 50 years
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
    public Date getDeletedAt() {
        return deletedAt != null ? new Date(deletedAt.getTime()) : null;
    }

    public void setDeletedAt(Date deletedAt) {
        if (deletedAt == null) {
            throw new IllegalArgumentException("deletedAt not specified");
        }
        firePropertyChange("deletedAt", this.deletedAt, deletedAt);

        this.deletedAt = new Date(deletedAt.getTime());
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

    @XmlTransient
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

    @XmlTransient
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

    @XmlTransient
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

    @XmlTransient
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
