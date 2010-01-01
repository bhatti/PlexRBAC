package com.plexobject.rbac.domain;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class PermissionTest {

    @Test
    public void testGetSet() {
        Permission p = new Permission();
        Assert.assertEquals(0, p.getId());
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

    @Test
    public void testGetSetContext() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.LESS_OR_EQUALS, "500"),
                new ContextProperty("time", ContextProperty.Type.TIME,
                        ContextProperty.Operator.IN_RANGE, "9:00am..5:00pm"),
                new ContextProperty("dept", ContextProperty.Type.STRING,
                        ContextProperty.Operator.CONTAINS, "sales")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "12:00pm",
                "dept", "SALES"));
    }

    @Test
    public void testBadGetSetContext() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.LESS_OR_EQUALS, "500"),
                new ContextProperty("time", ContextProperty.Type.TIME,
                        ContextProperty.Operator.IN_RANGE, "9:00am..5:00pm"),
                new ContextProperty("dept", ContextProperty.Type.STRING,
                        ContextProperty.Operator.CONTAINS, "sales")));
        Assert.assertFalse(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "SALES"));
        Assert.assertFalse(p.impliesContext("amount", "501", "time", "12:00pm",
                "dept", "SALES"));
        Assert.assertFalse(p.impliesContext("amount", "100", "time", "12:00pm",
                "dept", "SALESx"));
    }

    @Test
    public void testContains() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("dept",
                ContextProperty.Type.STRING, ContextProperty.Operator.CONTAINS,
                "sales")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sale"));
    }

    @Test
    public void testEquals() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("dept",
                ContextProperty.Type.STRING, ContextProperty.Operator.EQUALS,
                "sales")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sales"));
        Assert.assertFalse(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sale"));
    }

    @Test
    public void testLessThan() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.LESS_THAN, "1000")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sales"));
        Assert.assertFalse(p.impliesContext("amount", "1000"));
    }

    @Test
    public void testLessThanOrEquals() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.LESS_OR_EQUALS, "1000")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sales"));
        Assert.assertTrue(p.impliesContext("amount", "1000"));
        Assert.assertFalse(p.impliesContext("amount", "1001"));

    }

    @Test
    public void testGreaterThan() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.GREATER_THAN, "1000")));
        Assert.assertFalse(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sales"));
        Assert.assertFalse(p.impliesContext("amount", "1000"));
        Assert.assertTrue(p.impliesContext("amount", "1001"));

    }

    @Test
    public void testGreaterThanOrEquals() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.GREATER_OR_EQUALS, "1000")));
        Assert.assertFalse(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sales"));
        Assert.assertTrue(p.impliesContext("amount", "1000"));
        Assert.assertTrue(p.impliesContext("amount", "1001"));

    }

    @Test
    public void testNotEquals() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("dept",
                ContextProperty.Type.STRING,
                ContextProperty.Operator.NOT_EQUALS, "sales")));
        Assert.assertFalse(p.impliesContext("dept", "sales"));
        Assert.assertTrue(p.impliesContext("dept", "sale"));
    }

    @Test
    public void testRange() {
        Permission p = new Permission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER, ContextProperty.Operator.IN_RANGE,
                "10..20")));
        Assert.assertFalse(p.impliesContext("amount", "9"));
        Assert.assertTrue(p.impliesContext("amount", "10"));
        Assert.assertTrue(p.impliesContext("amount", "15"));
        Assert.assertTrue(p.impliesContext("amount", "20"));
        Assert.assertFalse(p.impliesContext("amount", "21"));

    }

}
