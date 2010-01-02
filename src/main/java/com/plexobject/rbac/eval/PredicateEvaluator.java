package com.plexobject.rbac.eval;

import java.util.Map;

public interface PredicateEvaluator {
    public boolean evaluate(String expression, Map<String, String> args);
}
