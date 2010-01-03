package com.plexobject.rbac.eval.js;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.plexobject.rbac.eval.PredicateEvaluator;

public class JavascriptEvaluator implements PredicateEvaluator {
    private ScriptEngineManager manager = new ScriptEngineManager();

    @Override
    public boolean evaluate(String expression, Map<String, String> args) {
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        ScriptContext newContext = new SimpleScriptContext();
        Bindings engineScope = newContext
                .getBindings(ScriptContext.ENGINE_SCOPE);

        for (Map.Entry<String, String> e : args.entrySet()) {
            engineScope.put(e.getKey(), e.getValue());
        }
        Object response = null;
        try {
            response = engine.eval(expression, newContext);
            if (!(response instanceof Boolean)) {
                final String type = response != null ? response.getClass()
                        .getName() : null;
                throw new RuntimeException("Unexpected response " + response
                        + ", type " + type + " by evaluating " + expression
                        + " with args " + args);
            }
            return (Boolean) response;
        } catch (ScriptException e) {
            throw new RuntimeException("failed to evaluate " + expression
                    + " with args " + args + " resulting in " + response, e);
        }
    }
}
