package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class AppMetricaJsInitializerInterfaceTest extends CommonTest {

    @Mock
    private AppMetricaProxy proxy;
    private AppMetricaInitializerJsInterface appMetricaInitializerJsInterface;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        appMetricaInitializerJsInterface = new AppMetricaInitializerJsInterface(proxy);
    }

    @Test
    public void init() {
        final String value = "event value";
        appMetricaInitializerJsInterface.init(value);
        verify(proxy).reportJsInitEvent(value);
    }
}
