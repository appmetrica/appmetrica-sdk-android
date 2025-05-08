package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;

public class CustomErrorTest extends CommonTest {

    @Test
    public void constructor() throws IllegalAccessException {
        String id = "id";
        RegularError regularError = mock(RegularError.class);
        CustomError customError = new CustomError(regularError, id);
        ObjectPropertyAssertions<CustomError> assertions = ObjectPropertyAssertions(customError);

        assertions.checkField("identifier", id);
        assertions.checkField("regularError", regularError);

        assertions.checkAll();
    }

}
