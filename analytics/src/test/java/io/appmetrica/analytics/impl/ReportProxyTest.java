package io.appmetrica.analytics.impl;

import android.os.Bundle;
import io.appmetrica.analytics.impl.service.ServiceDataReporter;
import io.appmetrica.analytics.impl.service.ServiceDataReporterHolder;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ReportProxyTest extends CommonTest {

    @Mock
    private ServiceDataReporter firstReporter;
    @Mock
    private ServiceDataReporter secondReporter;
    @Mock
    private Bundle bundle;
    @Mock
    private ServiceDataReporterHolder serviceDataReporterHolder;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private final ReportProxy reportProxy = new ReportProxy();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getServiceDataReporterHolder()).thenReturn(serviceDataReporterHolder);
    }

    @Test
    public void proxyReport() {
        final int type = 1;
        when(serviceDataReporterHolder.getServiceDataReporters(type))
            .thenReturn(Arrays.asList(firstReporter, secondReporter));

        reportProxy.proxyReport(type, bundle);

        verify(firstReporter).reportData(type, bundle);
        verify(secondReporter).reportData(type, bundle);
    }

    @Test
    public void proxyReportNoReporters() {
        final int type = 1;

        reportProxy.proxyReport(type, bundle);
        verifyNoInteractions(firstReporter, secondReporter);
    }
}
