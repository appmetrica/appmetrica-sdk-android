package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class MainReporterApiConsumerProviderTest extends CommonTest {

    @Mock
    private IMainReporter mainReporter;
    @Mock
    private DeeplinkConsumer deeplinkConsumer;
    private MainReporterApiConsumerProvider mainReporterApiConsumerProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mainReporterApiConsumerProvider = new MainReporterApiConsumerProvider(mainReporter, deeplinkConsumer);
    }

    @Test
    public void getMainReporter() {
        assertThat(mainReporterApiConsumerProvider.getMainReporter()).isSameAs(mainReporter);
    }

    @Test
    public void getDeeplinkConsumer() {
        assertThat(mainReporterApiConsumerProvider.getDeeplinkConsumer()).isSameAs(deeplinkConsumer);
    }
}
