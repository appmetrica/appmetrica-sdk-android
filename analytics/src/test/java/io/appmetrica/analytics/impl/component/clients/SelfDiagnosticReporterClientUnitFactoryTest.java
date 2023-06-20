package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelfDiagnosticReporterClientUnitFactoryTest extends ClientUnitFactoryBaseTest {

    @Mock
    private RegularDispatcherComponent mRegularDispatcherComponent;

    @Before
    public void setUp() {
        super.setUp();
        when(mComponentsRepository.getRegularComponentIfExists(any(ComponentId.class))).thenReturn(mRegularDispatcherComponent);
    }

    @Test
    public void testCreateClientUnit() {
        super.testCreateClientUnit(SelfDiagnosticClientUnit.class, ComponentId.class);
    }

    @Override
    protected ClientUnitFactory createClientUnitFactory() {
        return new SelfDiagnosticReporterClientUnitFactory();
    }

    @Override
    protected void verifyGetOrCreateComponentCall(ArgumentCaptor<ComponentId> captor) {
        verify(mComponentsRepository).getRegularComponentIfExists(captor.capture());
    }
}
