package io.appmetrica.analytics.snapshot.impl.utils;

import io.appmetrica.analytics.impl.CoverageUtils;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.testutils.CommonTest;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DebugAssertCoverageTest extends CommonTest {

    @Test
    public void debugAssertTestCoverage() {
        List<String> actual = CoverageUtils.getTestMethodNames(DebugAssertTest.class);
        List<String> expected = expectedCoveredMethods();
        assertThat(actual)
                .overridingErrorMessage("Methods of " + actual + " do not cover the following cases: " + expected)
                .size().isGreaterThanOrEqualTo(expected.size());
    }

    private List<String> expectedCoveredMethods() {
        List<String> result = new ArrayList<String>();
        for (Method method : DebugAssert.class.getDeclaredMethods()) {
            if (!Modifier.isPrivate(method.getModifiers())) {
                result.add(method.getName() + " that should fail");
                result.add(method.getName() + " that should succeed");
            }
        }
        return result;
    }

}
