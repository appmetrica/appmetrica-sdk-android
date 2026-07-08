package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.reporter.ManualReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.impl.TestsData.TEST_ENVIRONMENT_KEY;
import static io.appmetrica.analytics.impl.TestsData.TEST_ENVIRONMENT_VALUE;
import static io.appmetrica.analytics.impl.TestsData.TEST_ERROR_ENVIRONMENT_KEY;
import static io.appmetrica.analytics.impl.TestsData.TEST_ERROR_ENVIRONMENT_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ManualReporterTest extends BaseReporterTest {

    private final ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey).build();
    @Mock
    private ReporterLifecycleListener reporterLifecycleListener;

    @Test
    public void testMainReporterListenerIsCalledInConstructor() {
        when(ClientServiceLocator.getInstance().getReporterLifecycleListener())
            .thenReturn(reporterLifecycleListener);
        final BaseReporter reporter = getReporter();
        verify(reporterLifecycleListener).onCreateManualReporter(
            eq(apiKey),
            any(ManualReporterContext.class),
            eq(reporter)
        );
    }

    @Override
    protected BaseReporter getReporter() {
        return new ManualReporter(
            mContext,
            mReportsHandler,
            config,
            mReporterEnvironment,
            mExtraMetaInfoRetriever,
            processNameProvider,
            unhandledExceptionConverter,
            regularErrorConverter,
            customErrorConverter,
            anrConverter,
            pluginErrorDetailsConverter
        );
    }

    @Test
    public void testReporterType() {
        assertThat(
            new ManualReporter(
                mContext,
                new ProcessConfiguration(mContext, null),
                ReporterConfig.newConfigBuilder(apiKey).build(),
                mReportsHandler
            ).getEnvironment().getReporterConfiguration().getReporterType()
        ).isEqualTo(CounterConfigurationReporterType.MANUAL);
    }

    @Test
    public void userProfileID() {
        String userProfileID = "user_profile_id";
        assertThat(createReporterWithProfileID(userProfileID).mReporterEnvironment.getInitialUserProfileID())
            .isEqualTo(userProfileID);
    }

    @Test
    public void userProfileIDIfNotSet() {
        assertThat(createReporterWithProfileID(null).mReporterEnvironment.getInitialUserProfileID()).isNull();
    }

    @Test
    public void testReportUnhandledExceptionCrash() {
        final UnhandledException exception = mock(UnhandledException.class);
        mReporter.reportUnhandledException(exception);
        verify(mReportsHandler).reportCrash(exception, mReporterEnvironment);
    }

    @Test
    public void applyAppEnvironmentFromReporterConfig() {
        ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey)
            .withAppEnvironmentValue(TEST_ENVIRONMENT_KEY, TEST_ENVIRONMENT_VALUE)
            .withAppEnvironmentValue(TEST_ERROR_ENVIRONMENT_KEY, TEST_ERROR_ENVIRONMENT_VALUE)
            .build();
        ManualReporter reporter = new ManualReporter(mContext, mProcessConfiguration, config, mReportsHandler);
        reporter.putAllToAppEnvironment(config.appEnvironment);
        verify(mReportsHandler).sendAppEnvironmentValue(
            eq(TEST_ENVIRONMENT_KEY),
            eq(TEST_ENVIRONMENT_VALUE),
            any(ReporterEnvironment.class)
        );
        verify(mReportsHandler).sendAppEnvironmentValue(
            eq(TEST_ERROR_ENVIRONMENT_KEY),
            eq(TEST_ERROR_ENVIRONMENT_VALUE),
            any(ReporterEnvironment.class)
        );
    }

    private ManualReporter createReporterWithProfileID(String userProfileID) {
        ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey)
            .withUserProfileID(userProfileID)
            .build();
        return new ManualReporter(mContext, mProcessConfiguration, config, mReportsHandler);
    }

}
