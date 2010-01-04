package com.plexobject.rbac.domain;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.sleepycat.persist.model.NotPersistent;
import com.sleepycat.persist.model.Persistent;

@Persistent
public abstract class BaseDomain {
    @NotPersistent
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean dirty;

    protected boolean isDirty() {
        return dirty;
    }

    protected void clearDirty() {
        this.dirty = false;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);

    }

    public void fireIndexedPropertyChange(String propertyName, int index,
            boolean oldValue, boolean newValue) {
        this.dirty = true;
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void fireIndexedPropertyChange(String propertyName, int index,
            int oldValue, int newValue) {
        this.dirty = true;
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void fireIndexedPropertyChange(String propertyName, int index,
            Object oldValue, Object newValue) {
        this.dirty = true;
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        this.dirty = true;
        pcs.firePropertyChange(evt);
    }

    public void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        this.dirty = true;
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        this.dirty = true;
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        this.dirty = true;
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
}
