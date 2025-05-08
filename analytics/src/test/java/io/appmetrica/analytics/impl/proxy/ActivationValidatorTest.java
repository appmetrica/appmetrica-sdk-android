package io.appmetrica.analytics.impl.proxy;

import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.impl.proxy.validation.ActivationValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ActivationValidatorTest extends CommonTest {

    private final AppMetricaFacadeProvider mProvider = mock(AppMetricaFacadeProvider.class);

    @Test(expected = ValidationException.class)
    public void testNonActivated() {
        doReturn(false).when(mProvider).isActivated();
        new ActivationValidator(mProvider).validate();
    }

    @Test
    public void testActivated() {
        doReturn(true).when(mProvider).isActivated();
        new ActivationValidator(mProvider).validate();
    }

}
