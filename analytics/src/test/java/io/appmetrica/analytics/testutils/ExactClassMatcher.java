package io.appmetrica.analytics.testutils;

import org.mockito.ArgumentMatcher;

public class ExactClassMatcher<T> implements ArgumentMatcher<T> {

    final Class<T> mClass;

    public ExactClassMatcher(Class<T> aClass) {
        mClass = aClass;
    }

    @Override
    public boolean matches(Object argument) {
        return argument.getClass() == mClass;
    }
}
