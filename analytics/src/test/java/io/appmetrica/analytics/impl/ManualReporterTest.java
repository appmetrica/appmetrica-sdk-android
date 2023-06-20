package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.reporter.ManualReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ManualReporterTest extends BaseReporterTest {

    private ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey).build();
    @Mock
    private ReporterLifecycleListener reporterLifecycleListener;

    @Test
    public void testMainReporterListenerIsCalledInConstructor() {
        when(ClientServiceLocator.getInstance().getReporterLifecycleListener())
                .thenReturn(reporterLifecycleListener);
        final BaseReporter reporter = getReporter();
        verify(reporterLifecycleListener).onCreateManualReporter(
                eq(apiKey),
                any(ManualReporterContext.class)
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
                processDetector,
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

    private ManualReporter createReporterWithProfileID(String userProfileID) {
        ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey)
                .withUserProfileID(userProfileID)
                .build();
        return new ManualReporter(mContext, mProcessConfiguration, config, mReportsHandler);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ReporterReportCustomEventEventTypeTests extends BaseReporterTest.ReporterReportCustomEventEventTypeTests {

        private final ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey).build();

        public ReporterReportCustomEventEventTypeTests(int eventType, int wantedNumberOfInvocations) {
            super(eventType, wantedNumberOfInvocations);
        }

        @Override
        public BaseReporter getReporter() {
            return new ManualReporter(
                    mContext,
                    mReportsHandler,
                    config,
                    mReporterEnvironment,
                    mock(ExtraMetaInfoRetriever.class),
                    processDetector,
                    mock(UnhandledExceptionConverter.class),
                    mock(RegularErrorConverter.class),
                    mock(CustomErrorConverter.class),
                    mock(AnrConverter.class),
                    mock(PluginErrorDetailsConverter.class)
            );
        }
    }
}
