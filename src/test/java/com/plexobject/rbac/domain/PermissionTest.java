package com.plexobject.rbac.domain;

import org.junit.Assert;
import org.junit.Test;

public class PermissionTest {

    @Test
    public void testGetSet() {
        Permission p = new Permission();
        Assert.assertNull(p.getId());
        Assert.assertNull(p.getOperation());
        Assert.assertNull(p.getTarget());
    }

    @Test
    public void testOperationEquals() {
        Permission p = new Permission("read", "database", null);

        Assert.assertTrue(p.impliesOperation("READ", "database"));
        Assert.assertFalse(p.impliesOperation("READ", "file"));
        Assert.assertFalse(p.impliesOperation("XREAD", "database"));
    }

    @Test
    public void testOperationRegex() {
        Permission p = new Permission("*", "database", null);

        Assert.assertTrue(p.impliesOperation("READ", "database"));
        Assert.assertTrue(p.impliesOperation("XREAD", "database"));
        Assert.assertFalse(p.impliesOperation("XREAD", "file"));
        p = new Permission("(write|read|update|delete)", "database", null);
        Assert.assertTrue(p.impliesOperation("read", "database"));
        Assert.assertTrue(p.impliesOperation("DELETE", "database"));
    }

}
