package io.appmetrica.analytics.network.impl;

import io.appmetrica.analytics.network.internal.NetworkClient;
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
                        NetworkClient.class,
                        new NetworkClient.Builder().build(),
                        Modifier.PRIVATE | Modifier.FINAL,
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
