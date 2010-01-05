package com.plexobject.rbac.domain;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Assert;
import org.junit.Test;

public class DomainTest {

    @Test
    public void testGetSetEmpty() {
        Domain app = new Domain();
        Assert.assertNull(app.getID());
        Assert.assertNull(app.getDescription());
        Assert.assertNotNull(app.getCreatedAt());
        Assert.assertNotNull(app.getUpdatedAt());
        Assert.assertNull(app.getCreatedBy());
        Assert.assertNull(app.getUpdatedBy());
        Assert.assertNull(app.getUpdatedIPAddress());
    }

    @Test
    public void testDate() {
        Domain app = new Domain();
        app.setCreatedBy("");
        app.setUpdatedBy("");
        app.setUpdatedIPAddress("");
        Assert.assertNull(app.getID());
        Assert.assertNull(app.getDescription());
        Assert.assertNotNull(app.getCreatedAt());
        Assert.assertNotNull(app.getUpdatedAt());
        Assert.assertNotNull(app.getCreatedBy());
        Assert.assertNotNull(app.getUpdatedBy());
        Assert.assertNotNull(app.getUpdatedIPAddress());
    }

    @Test
    public void testDirty() {
        Domain app = new Domain();
        Assert.assertFalse(app.isDirty());
        app.setUpdatedIPAddress("");
        Assert.assertTrue(app.isDirty());
    }

    @Test
    public void testPCS() {
        Domain app = new Domain();
        final int[] calls = new int[1];
        final PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                calls[0]++;
            }
        };
        app.addPropertyChangeListener(listener);
        app.setUpdatedIPAddress("");
        Assert.assertEquals(1, calls[0]);
    }
}
