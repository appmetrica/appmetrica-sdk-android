package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainOrCrashReporterProviderTest extends CommonTest {

    @Mock
    private IReporterExtended mReporter;
    @Mock
    private IReporterFactoryProvider mReporterFactoryProvider;
    private final String mApiKey = TestsData.generateApiKey();
    private final AppMetricaConfig mConfig = AppMetricaConfig.newConfigBuilder(mApiKey).build();
    private UnhandledSituationReporterProvider mReporterProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReporterFactory reporterFactory = mock(ReporterFactory.class);
        when(mReporterFactoryProvider.getReporterFactory()).thenReturn(reporterFactory);
        when(reporterFactory.getUnhandhedSituationReporter(mConfig)).thenReturn(mReporter);
        mReporterProvider = new MainOrCrashReporterProvider(mReporterFactoryProvider, mConfig);
    }

    @Test
    public void getReporter() {
        assertThat(mReporterProvider.getReporter()).isEqualTo(mReporter);
    }
}
