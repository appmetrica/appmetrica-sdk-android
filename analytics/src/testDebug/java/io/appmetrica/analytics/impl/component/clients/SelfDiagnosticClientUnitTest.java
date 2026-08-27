package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class SelfDiagnosticClientUnitTest extends CommonTest {

    @Mock
    private ServiceEvent mServiceEvent;
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
        new SelfDiagnosticClientUnit(mRegularDispatcherComponent).handle(mServiceEvent, mCommonArguments);
        verify(mRegularDispatcherComponent).handleReport(mServiceEvent, mCommonArguments);
    }

    @Test
    public void testComponentUnitNullDoesNotThrow() {
        new SelfDiagnosticClientUnit(null).handle(mServiceEvent, mCommonArguments);
    }
}
