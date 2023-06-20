package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class RegularClientUnitTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();
    @Mock
    private RegularDispatcherComponent<ComponentUnit> mComponentUnit;
    @Mock
    private CounterReport mReport;
    @Mock
    private CommonArguments mClientConfiguration;

    private RegularClientUnit mClientUnit;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mClientUnit = new RegularClientUnit(RuntimeEnvironment.getApplication(), mComponentUnit);
    }

    @Test
    public void testReport() {
        mClientUnit.handle(mReport, mClientConfiguration);
        verify(mComponentUnit, times(1)).handleReport(mReport, mClientConfiguration);
    }

}
