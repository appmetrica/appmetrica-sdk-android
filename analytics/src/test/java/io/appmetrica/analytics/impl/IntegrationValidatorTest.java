package io.appmetrica.analytics.impl;

import android.os.Build;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class IntegrationValidatorTest extends CommonTest {

    private static final String COUNTER_CONFIGURATION_CLASS = "io.appmetrica.analytics.CounterConfiguration";

    @Rule
    public final MockedStaticRule<IntegrationValidator> sValidator = new MockedStaticRule<>(IntegrationValidator.class);

    @Before
    public void setUp() throws Exception {
        sValidator.getStaticMock().when(new MockedStatic.Verification() {
            @Override
            public void apply() {
                IntegrationValidator.checkValidityOfAppMetricaConfiguration();
            }
        }).thenCallRealMethod();
        sValidator.getStaticMock().when(new MockedStatic.Verification() {
            @Override
            public void apply() {
                IntegrationValidator.checkImportantAppMetricaClasses();
            }
        }).thenCallRealMethod();
    }

    @Test
    public void testHasCounterConfiguration() {
        when(IntegrationValidator.isClassExisting(COUNTER_CONFIGURATION_CLASS)).thenReturn(true);
        IntegrationValidator.checkValidityOfAppMetricaConfiguration();
    }

    @Test(expected = IntegrationValidator.IntegrationException.class)
    public void testDoesNotHaveCounterConfiguration() {
        when(IntegrationValidator.isClassExisting(COUNTER_CONFIGURATION_CLASS)).thenReturn(false);
        IntegrationValidator.checkValidityOfAppMetricaConfiguration();
    }
}
