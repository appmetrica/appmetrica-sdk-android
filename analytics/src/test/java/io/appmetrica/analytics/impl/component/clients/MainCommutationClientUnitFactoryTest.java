package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.component.CommutationComponentId;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.DispatcherComponentFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainCommutationClientUnitFactoryTest extends ClientUnitFactoryBaseTest {

    @Before
    public void setUp() {
        super.setUp();
        when(mComponentsRepository
                .getOrCreateCommutationComponent(any(ComponentId.class), same(mCommonArguments), any(DispatcherComponentFactory.class))
        ).thenReturn(mock(CommutationDispatcherComponent.class));
    }

    @Override
    protected ClientUnitFactory createClientUnitFactory() {
        return new MainCommutationClientUnitFactory();
    }

    @Override
    protected void verifyGetOrCreateComponentCall(ArgumentCaptor<ComponentId> captor) {
        verify(mComponentsRepository).getOrCreateCommutationComponent(captor.capture(), same(mCommonArguments), any(DispatcherComponentFactory.class));
    }

    @Override
    protected String getExpectedApiKey() {
        return null;
    }

    @Test
    public void testCreateClientUnit() {
        super.testCreateClientUnit(CommutationClientUnit.class, CommutationComponentId.class);
    }
}
