package com.plexobject.rbac.eval.simple;

import org.junit.Assert;
import org.junit.Test;

import com.plexobject.rbac.eval.PredicateEvaluator;
import com.plexobject.rbac.utils.IDUtils;

public class SimpleEvaluatorTest {
    PredicateEvaluator evaluator = new SimpleEvaluator();

    @Test
    public void testGetSetContext() {
        String expr = "amount <= 500 && dept == 'SALES' && time between 8:00am..5:00pm";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100", "time", "12:00pm", "dept", "SALES")));
    }

    @Test
    public void testBadGetSetContext() {
        String expr = "amount <= 500 && dept == 'SALES' && time between 8:00am..5:00pm";

        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100", "time", "6:00pm", "dept", "SALES")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "501", "time", "12:00pm", "dept", "SALES")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100", "time", "12:00pm", "dept", "SALESx")));
    }

    @Test
    public void testContains() {
        String expr = "dept in sales";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils
                .toMap("dept", "SALE")));
    }

    @Test
    public void testEquals() {
        String expr = "dept == sales dept";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "SALEs dept")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "SALEs  dept")));
    }

    @Test
    public void testLessThan() {
        String expr = "amount < 1000";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1000")));
    }

    @Test
    public void testLessThanOrEquals() {
        String expr = "amount <= 1000";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1000")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1001")));
    }

    @Test
    public void testGreaterThan() {
        String expr = "amount > 1000";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1001")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1000")));
    }

    @Test
    public void testGreaterThanOrEquals() {
        String expr = "amount >= 1000";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1000")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "999")));
    }

    @Test
    public void testNotEquals() {
        String expr = "dept != sales";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "SALEs dept")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "SALEs")));
    }

    @Test
    public void testRange() {
        String expr = "amount between 10..20";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils
                .toMap("amount", "10")));
        Assert.assertTrue(evaluator.evaluate(expr, IDUtils
                .toMap("amount", "10")));
        Assert.assertTrue(evaluator.evaluate(expr, IDUtils
                .toMap("amount", "20")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "21")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils
                .toMap("amount", "9")));
    }
}
