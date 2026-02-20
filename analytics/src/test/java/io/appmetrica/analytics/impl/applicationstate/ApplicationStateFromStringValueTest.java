package io.appmetrica.analytics.impl.applicationstate;

import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApplicationStateFromStringValueTest extends CommonTest {

    private final String mInputValue;
    private final ApplicationState mExpectedValue;

    public ApplicationStateFromStringValueTest(String inputValue,
                                               ApplicationState expectedValue) {
        mInputValue = inputValue;
        mExpectedValue = expectedValue;
    }

    @Parameters(name = "for input \"{0}\"")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"visible", ApplicationState.VISIBLE},
            {"background", ApplicationState.BACKGROUND},
            {"unknown", ApplicationState.UNKNOWN},
            {"", ApplicationState.UNKNOWN},
            {null, ApplicationState.UNKNOWN}
        });
    }

    @Test
    public void testFromStringValue() {
        assertThat(ApplicationState.fromString(mInputValue)).isEqualTo(mExpectedValue);
    }
}
