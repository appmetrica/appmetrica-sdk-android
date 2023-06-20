package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class AppMetricaJsInterfaceTest extends CommonTest {

    @Mock
    private AppMetricaProxy proxy;
    private AppMetricaJsInterface appMetricaJsInterface;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        appMetricaJsInterface = new AppMetricaJsInterface(proxy);
    }

    @Test
    public void reportEvent() {
        final String name = "event name";
        final String value = "event value";
        appMetricaJsInterface.reportEvent(name, value);
        verify(proxy).reportJsEvent(name, value);
    }
}
