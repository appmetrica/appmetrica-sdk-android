package io.appmetrica.analytics.testutils;

import java.util.List;
import java.util.function.Predicate;
import org.junit.Test;

public abstract class BaseToStringTest extends CommonTest {

    private Class clazz;
    private Object actualValue;
    private int modifierPreconditions;

    public BaseToStringTest(Object clazz, Object actualValue, int modifierPreconditions, String additionalDescription)
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

    @Test
    public void toStringContainsAllFields() throws Exception {
        List<Predicate<String>> extractedFieldAndValues =
            ToStringTestUtils.extractFieldsAndValues(clazz, actualValue, modifierPreconditions);
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues);
    }
}
