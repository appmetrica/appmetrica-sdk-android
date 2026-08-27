package io.appmetrica.analytics.networktasks.impl;

import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import java.util.Arrays;
import java.util.Collection;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

public class HashCodeEqualsTest {
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class AllFieldsTest {

        private final Class mClazz;

        public AllFieldsTest(final Class clazz) {
            mClazz = clazz;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "class = {0}")
        public static Collection<Object[]> params() {
            return Arrays.asList(new Object[][]{
                    {RetryPolicyConfig.class}
            });
        }

        @Test
        public void testEqualsAndHashCode() {
            EqualsVerifier.forClass(mClazz)
                    .usingGetClass()
                    .verify();
        }
    }
}
