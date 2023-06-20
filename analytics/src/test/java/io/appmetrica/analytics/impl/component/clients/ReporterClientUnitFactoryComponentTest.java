package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.component.ReporterComponentUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReporterClientUnitFactoryComponentTest extends ComponentUnitFactoryBaseTest {

    @Override
    protected ComponentUnitFactory createComponentUnitFactory() {
        return new ReporterClientUnitFactory();
    }

    @Test
    public void testCreateComponentUnit() {
        super.testCreateComponentUnit(ReporterComponentUnit.class);
    }
}
