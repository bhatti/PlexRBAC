package com.plexobject.rbac.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.plexobject.rbac.domain.Identifiable;

public class IDUtils {
    @SuppressWarnings("unchecked")
    public static String getIdsAsString(
            Collection<? extends Identifiable> objects) {
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

    public static Collection<Integer> getIdsAsIntegers(
            Collection<? extends Identifiable<Integer>> objects) {
        Collection<Integer> ids = new ArrayList<Integer>();
        for (Identifiable<Integer> o : objects) {
            ids.add(o.getId());
        }
        return ids;
    }

    public static Collection<String> getIdsAsString(
            Collection<? extends Identifiable<String>> objects) {
        Collection<String> ids = new ArrayList<String>();
        for (Identifiable<String> o : objects) {
            ids.add(o.getId());
        }
        return ids;
    }

    public static Map<String, Object> toMap(final Object... keyValues) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i].toString(), keyValues[i + 1]);
        }
        return map;
    }
}
