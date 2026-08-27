package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class CustomErrorTest extends CommonTest {

    @Test
    public void constructor() throws IllegalAccessException {
        String id = "id";
        RegularError regularError = mock(RegularError.class);
        CustomError customError = new CustomError(regularError, id);
        ObjectPropertyAssertions<CustomError> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(customError);

        assertions.checkField("identifier", id);
        assertions.checkField("regularError", regularError);

        assertions.checkAll();
    }

}
