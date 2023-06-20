package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.component.MainReporterComponentUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MainReporterClientFactoryComponentTest extends ComponentUnitFactoryBaseTest {

    @Test
    public void testCreateComponentUnit() {
        super.testCreateComponentUnit(MainReporterComponentUnit.class);
    }

    @Override
    protected ComponentUnitFactory createComponentUnitFactory() {
        return new MainReporterClientFactory();
    }
}
