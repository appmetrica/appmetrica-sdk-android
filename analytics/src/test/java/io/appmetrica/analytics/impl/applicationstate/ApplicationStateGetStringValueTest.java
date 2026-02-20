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
public class ApplicationStateGetStringValueTest extends CommonTest {

    private final ApplicationState mInputValue;
    private final String mExpectedValue;

    public ApplicationStateGetStringValueTest(ApplicationState inputValue,
                                              String expectedValue) {
        mInputValue = inputValue;
        mExpectedValue = expectedValue;
    }

    @Parameters(name = "for input {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {ApplicationState.UNKNOWN, "unknown"},
            {ApplicationState.VISIBLE, "visible"},
            {ApplicationState.BACKGROUND, "background"}
        });
    }

    @Test
    public void testGetStringValue() {
        assertThat(mInputValue.getStringValue()).isEqualTo(mExpectedValue);
    }
}
