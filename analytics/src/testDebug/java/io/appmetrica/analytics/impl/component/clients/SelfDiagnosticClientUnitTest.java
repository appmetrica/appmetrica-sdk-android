package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.CoreServiceEvent;
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
    private CoreServiceEvent serviceEvent;
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
        new SelfDiagnosticClientUnit(mRegularDispatcherComponent).handle(serviceEvent, mCommonArguments);
        verify(mRegularDispatcherComponent).handleReport(serviceEvent, mCommonArguments);
    }

    @Test
    public void testComponentUnitNullDoesNotThrow() {
        new SelfDiagnosticClientUnit(null).handle(serviceEvent, mCommonArguments);
    }
}
