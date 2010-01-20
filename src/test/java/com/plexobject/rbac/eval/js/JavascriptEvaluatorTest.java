package com.plexobject.rbac.eval.js;

import org.junit.Assert;
import org.junit.Test;

import com.plexobject.rbac.eval.PredicateEvaluator;
import com.plexobject.rbac.utils.IDUtils;

public class JavascriptEvaluatorTest {
    PredicateEvaluator evaluator = new JavascriptEvaluator();

    @Test
    public void testGetSetContext() {
        String expr = "var time=new Date(2010, 0, 1, 12, 0, 0, 0);\namount <= 500 && dept == 'SALES' && time.getHours() >= 8 && time.getHours() <= 17";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100", "dept", "SALES")));
    }

    @Test
    public void testBadGetSetContext() {
        String expr = "var time=new Date(2010, 0, 1, 18, 0, 0, 0);\namount <= 500 && dept == 'SALES' && time.getHours() >= 8 && time.getHours() <= 17";

        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100", "dept", "SALES")));
        expr = "var time=new Date(2010, 0, 1, 12, 0, 0, 0);\namount <= 500 && dept == 'SALES' && time.getHours() >= 8 && time.getHours() <= 17";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "500", "dept", "SALES")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "501", "dept", "SALES")));

        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100", "dept", "sales")));
    }

    @Test
    public void testContains() {
        String expr = "dept.search(/sale?/i) != -1";
        Assert.assertTrue(evaluator.evaluate(expr, IDUtils
                .toMap("dept", "SALE")));

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "SALES")));
    }

    @Test
    public void testEquals() {
        String expr = "dept.search(/sale. dept/i) != -1";

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
        String expr = "dept != 'sales'";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "SALEs dept")));
        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("dept",
                "sales")));
    }

    @Test
    public void testRange() {
        String expr = "amount >= 10 && amount <= 20";

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

    @Test
    public void testOr() {
        String expr = "amount < 1000 || approver == 'shahbhat'";

        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "100")));
        Assert.assertTrue(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1000", "approver", "shahbhat")));

        Assert.assertFalse(evaluator.evaluate(expr, IDUtils.toMap("amount",
                "1000", "approver", "bhatsha")));
    }
}
