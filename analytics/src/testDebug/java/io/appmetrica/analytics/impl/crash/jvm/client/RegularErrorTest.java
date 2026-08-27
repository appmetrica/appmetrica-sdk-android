package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RegularErrorTest extends CommonTest {

    @Test
    public void construction() throws IllegalAccessException {
        UnhandledException exception = mock(UnhandledException.class);
        String message = "message";

        RegularError error = new RegularError(message, exception);

        ObjectPropertyAssertions<RegularError> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(error);

        assertions.checkField("message", message).checkField("exception", exception).checkAll();
    }

    @Test
    public void constructionNullable() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        RegularError error = new RegularError(null, null);

        ObjectPropertyAssertions<RegularError> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(error);

        assertions.checkFieldsAreNull("message", "exception").checkAll();
    }
}
