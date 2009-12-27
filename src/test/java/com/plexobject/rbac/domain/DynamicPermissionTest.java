package com.plexobject.rbac.domain;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class DynamicPermissionTest {

    @Test
    public void testGetSetContext() {
        DynamicPermission p = new DynamicPermission();
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
        DynamicPermission p = new DynamicPermission();
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
        DynamicPermission p = new DynamicPermission();
        p.setContext(Arrays.asList(new ContextProperty("dept",
                ContextProperty.Type.STRING, ContextProperty.Operator.CONTAINS,
                "sales")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sale"));
    }

    @Test
    public void testEquals() {
        DynamicPermission p = new DynamicPermission();
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
        DynamicPermission p = new DynamicPermission();
        p.setContext(Arrays.asList(new ContextProperty("amount",
                ContextProperty.Type.NUMBER,
                ContextProperty.Operator.LESS_THAN, "1000")));
        Assert.assertTrue(p.impliesContext("amount", "100", "time", "6:00pm",
                "dept", "sales"));
        Assert.assertFalse(p.impliesContext("amount", "1000"));
    }

    @Test
    public void testLessThanOrEquals() {
        DynamicPermission p = new DynamicPermission();
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
        DynamicPermission p = new DynamicPermission();
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
        DynamicPermission p = new DynamicPermission();
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
        DynamicPermission p = new DynamicPermission();
        p.setContext(Arrays.asList(new ContextProperty("dept",
                ContextProperty.Type.STRING,
                ContextProperty.Operator.NOT_EQUALS, "sales")));
        Assert.assertFalse(p.impliesContext("dept", "sales"));
        Assert.assertTrue(p.impliesContext("dept", "sale"));
    }

    @Test
    public void testRange() {
        DynamicPermission p = new DynamicPermission();
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
