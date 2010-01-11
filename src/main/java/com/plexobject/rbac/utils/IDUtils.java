package com.plexobject.rbac.utils;

import java.util.Collection;

import com.plexobject.rbac.domain.Identifiable;

public class IDUtils {
    @SuppressWarnings("unchecked")
    public static String getIds(Collection<? extends Identifiable> objects) {
        StringBuilder sb = new StringBuilder("[");
        for (Identifiable o : objects) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append("\"" + o.getId() + "\"");
        }
        sb.append("]");
        return sb.toString();
    }
}
