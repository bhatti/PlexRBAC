package com.plexobject.rbac.eval.simple;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.rbac.eval.PredicateEvaluator;

public class SimpleEvaluator implements PredicateEvaluator {
    private static final Logger LOGGER = Logger
            .getLogger(SimpleEvaluator.class);

    @Override
    public boolean evaluate(String rawExpr, Map<String, String> args) {
        Collection<Expression> exprs = Expression.parse(rawExpr);
        for (Expression expr : exprs) {
            String value = args.get(expr.getName());
            if (!expr.evaluate(value)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Expression " + expr + " failed against "
                            + value + " with name " + expr.getName()
                            + " in args " + args);
                }
                return false;
            }
        }
        return true;
    }
}
