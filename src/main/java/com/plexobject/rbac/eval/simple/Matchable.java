package com.plexobject.rbac.eval.simple;

public interface Matchable {
    boolean doesMatch(final String first, final String second, final Type type);
}
