package io.appmetrica.analytics.networktasks.impl;

import io.appmetrica.analytics.networktasks.internal.DefaultResponseParser;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ToStringTest {

    private Class clazz;
    private Object actualValue;
    private int modifierPreconditions;

    public ToStringTest(Object clazz, Object actualValue, int modifierPreconditions, String additionalDescription)
            throws Exception {
        if (clazz instanceof Class) {
            this.clazz = ((Class) clazz);
        } else if (clazz instanceof String) {
            this.clazz = Class.forName(((String) clazz));
        } else {
            throw new IllegalArgumentException("Clazz must be instance of Class or String");
        }
        this.actualValue = actualValue;
        this.modifierPreconditions = modifierPreconditions;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][]{
                {
                        RetryPolicyConfig.class,
                        new RetryPolicyConfig(66, 77),
                        Modifier.PRIVATE | Modifier.FINAL,
                        ""
                },
                {
                        DefaultResponseParser.Response.class,
                        new DefaultResponseParser.Response("my status"),
                        Modifier.PUBLIC | Modifier.FINAL,
                        ""
                },
        });
    }

    @Test
    public void toStringContainsAllFields() throws Exception {
        List<Predicate<String>> extractedFieldAndValues =
                ToStringTestUtils.extractFieldsAndValues(clazz, actualValue, modifierPreconditions);
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues);
    }
}
