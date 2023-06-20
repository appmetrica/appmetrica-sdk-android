package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class CoverageUtils {

    @NonNull
    public static Set<Method> getTestMethods(@NonNull Class<?> clazz) {
        Set<Method> result = new HashSet<Method>();
        for (Method method : clazz.getMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                result.add(method);
            }
        }
        return result;
    }

    @NonNull
    public static List<String> getTestMethodNames(@NonNull Class<?> clazz) {
        List<String> result = new ArrayList<String>();
        for (Method method : clazz.getMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                result.add(method.getName());
            }
        }
        return result;
    }
}
