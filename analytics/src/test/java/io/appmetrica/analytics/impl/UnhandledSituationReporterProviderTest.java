package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UnhandledSituationReporterProviderTest extends CommonTest {

    @Mock
    private IReporterExtended mReporter;
    @Mock
    private IReporterFactoryProvider mReporterFactoryProvider;
    private final String mApiKey = TestsData.generateApiKey();
    private UnhandledSituationReporterProvider mReporterProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReporterFactory reporterFactory = mock(ReporterFactory.class);
        when(mReporterFactoryProvider.getReporterFactory()).thenReturn(reporterFactory);
        when(reporterFactory.getOrCreateReporter(argThat(new ArgumentMatcher<ReporterConfig>() {
            @Override
            public boolean matches(ReporterConfig argument) {
                return argument.apiKey.equals(mApiKey);
            }
        }))).thenReturn(mReporter);
    }

    @Test
    public void testCreateReporter() {
        mReporterProvider = new UnhandledSituationReporterProvider(mReporterFactoryProvider, mApiKey);
        assertThat(mReporterProvider.getReporter()).isEqualTo(mReporter);
    }

}
