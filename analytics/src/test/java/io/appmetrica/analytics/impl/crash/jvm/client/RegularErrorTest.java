package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.crash.jvm.client.RegularError;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;

public class RegularErrorTest extends CommonTest {

    @Test
    public void construction() throws IllegalAccessException {
        UnhandledException exception = mock(UnhandledException.class);
        String message = "message";

        RegularError error = new RegularError(message, exception);

        ObjectPropertyAssertions<RegularError> assertions = ObjectPropertyAssertions(error);

        assertions.checkField("message", message).checkField("exception", exception).checkAll();
    }

    @Test
    public void constructionNullable() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        RegularError error = new RegularError(null, null);

        ObjectPropertyAssertions<RegularError> assertions = ObjectPropertyAssertions(error);

        assertions.checkFieldsAreNull("message", "exception").checkAll();
    }
}
