package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

public class SelfReportingFactoryTest extends CommonTest {

    @RunWith(RobolectricTestRunner.class)
    public static class ClientUnitFactoryTest extends ReporterClientUnitFactoryTest {

    }

    @RunWith(RobolectricTestRunner.class)
    public static class ComponentFactoryTest extends ReporterClientUnitFactoryComponentTest {

    }
}
