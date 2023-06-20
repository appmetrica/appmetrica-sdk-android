package io.appmetrica.analytics.testutils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ContainsArgumentMessageMatcher extends BaseMatcher<String> {
    private final String mPattern;

    public ContainsArgumentMessageMatcher(String pattern) {
        mPattern = pattern;
    }

    @Override
    public boolean matches(Object o) {
        return ((String) o).contains(mPattern);
    }

    public void describeTo(Description description) {

    }
}
