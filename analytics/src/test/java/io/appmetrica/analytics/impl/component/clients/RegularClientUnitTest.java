package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RegularClientUnitTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

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
        mClientUnit = new RegularClientUnit(contextRule.getContext(), mComponentUnit);
    }

    @Test
    public void testReport() {
        mClientUnit.handle(mReport, mClientConfiguration);
        verify(mComponentUnit, times(1)).handleReport(mReport, mClientConfiguration);
    }

}
