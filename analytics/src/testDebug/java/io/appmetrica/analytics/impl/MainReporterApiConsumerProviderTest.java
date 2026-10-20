package io.appmetrica.analytics.impl;

import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class MainReporterApiConsumerProviderTest extends CommonTest {

    @Mock
    private MainReporter mainReporter;
    private MainReporterApiConsumerProvider mainReporterApiConsumerProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mainReporterApiConsumerProvider = new MainReporterApiConsumerProvider(mainReporter);
    }

    @Test
    public void getMainReporter() {
        assertThat(mainReporterApiConsumerProvider.getMainReporter()).isSameAs(mainReporter);
    }

}
