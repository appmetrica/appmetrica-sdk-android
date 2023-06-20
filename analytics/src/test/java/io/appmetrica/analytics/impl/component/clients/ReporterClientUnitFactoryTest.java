package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.DispatcherComponentFactory;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterClientUnitFactoryTest extends ClientUnitFactoryBaseTest {

    @Before
    public void setUp() {
        super.setUp();
        when(mComponentsRepository
                .getOrCreateRegularComponent(any(ComponentId.class), same(mCommonArguments), any(DispatcherComponentFactory.class))
        ).thenReturn(mock(RegularDispatcherComponent.class));
    }

    @Test
    public void testCreateClientUnit() {
        super.testCreateClientUnit(RegularClientUnit.class, ComponentId.class);
    }

    @Override
    protected ClientUnitFactory createClientUnitFactory() {
        return new ReporterClientUnitFactory();
    }
}
