package com.plexobject.rbac.domain;

import org.junit.Assert;
import org.junit.Test;

public class PermissionTest {

    @Test
    public void testGetSet() {
        Permission p = new Permission();
        Assert.assertNull(p.getID());
        Assert.assertNull(p.getApplicationName());
        Assert.assertNull(p.getOperation());
        Assert.assertNull(p.getTarget());
    }

    @Test
    public void testOperationEquals() {
        Permission p = new Permission();
        p.setOperation("read");
        Assert.assertTrue(p.impliesOperation("READ"));
        Assert.assertFalse(p.impliesOperation("XREAD"));
    }

    @Test
    public void testOperationRegex() {
        Permission p = new Permission();
        p.setOperation("*");
        Assert.assertTrue(p.impliesOperation("READ"));
        Assert.assertTrue(p.impliesOperation("XREAD"));
        p.setOperation("(write|read|update|delete)");
        Assert.assertTrue(p.impliesOperation("read"));
        Assert.assertTrue(p.impliesOperation("DELETE"));
    }

}
