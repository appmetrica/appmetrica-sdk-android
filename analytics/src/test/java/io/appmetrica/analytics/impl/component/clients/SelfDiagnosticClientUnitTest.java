package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SelfDiagnosticClientUnitTest extends CommonTest {

    @Mock
    private CounterReport mCounterReport;
    @Mock
    private RegularDispatcherComponent mRegularDispatcherComponent;
    private CommonArguments mCommonArguments;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mCommonArguments = CommonArgumentsTestUtils.createMockedArguments();
    }

    @Test
    public void testComponentUnitNotNull() {
        new SelfDiagnosticClientUnit(mRegularDispatcherComponent).handle(mCounterReport, mCommonArguments);
        verify(mRegularDispatcherComponent).handleReport(mCounterReport, mCommonArguments);
    }

    @Test
    public void testComponentUnitNullDoesNotThrow() {
        new SelfDiagnosticClientUnit(null).handle(mCounterReport, mCommonArguments);
    }
}
