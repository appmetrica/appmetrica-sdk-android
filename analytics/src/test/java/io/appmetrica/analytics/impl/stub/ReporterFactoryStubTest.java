package io.appmetrica.analytics.impl.stub;

import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class ReporterFactoryStubTest extends CommonTest {

    @Mock
    private AppMetricaConfig appMetricaConfig;
    @Mock
    private ReporterConfig reporterInternalConfig;

    private ReporterFactoryStub reporterFactoryStub;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        reporterFactoryStub = new ReporterFactoryStub();
    }

    @Test
    public void buildMainReporter() {
        assertThat(reporterFactoryStub.buildMainReporter(appMetricaConfig, true))
            .isNotNull()
            .isInstanceOf(MainReporterStub.class);
        verifyNoMoreInteractions(appMetricaConfig);
    }

    @Test
    public void activateReporter() {
        reporterFactoryStub.activateReporter(reporterInternalConfig);
        verifyNoMoreInteractions(reporterInternalConfig);
    }

    @Test
    public void getOrCreateReporter() {
        assertThat(reporterFactoryStub.getOrCreateReporter(reporterInternalConfig))
                .isNotNull()
                .isInstanceOf(ReporterExtendedStub.class);
        verifyNoMoreInteractions(reporterInternalConfig);
    }

    @Test
    public void getMainOrCrashReporter() {
        assertThat(reporterFactoryStub.getMainOrCrashReporter(appMetricaConfig))
                .isNotNull()
                .isInstanceOf(ReporterExtendedStub.class);
    }

    @Test
    public void getReporterFactory() {
        assertThat(reporterFactoryStub.getReporterFactory()).isEqualTo(reporterFactoryStub);
    }
}
