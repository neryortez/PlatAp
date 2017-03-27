package io.github.rathn.platap.utils;

import java.util.List;

public class TypeUtils {
    public static boolean isEmpty(String s) {
        return s == null ? true : s.isEmpty();
    }

    public static boolean isEmpty(List<?> list) {
        return list == null ? true : list.isEmpty();
    }
}
