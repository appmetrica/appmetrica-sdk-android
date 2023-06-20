package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.CrashToFileWriter;
import io.appmetrica.analytics.impl.crash.UnhandledExceptionEventFormer;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.reporter.CrashReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CrashReporterTest extends BaseReporterTest {

    @Mock
    private CrashToFileWriter mCrashToFileWriter;
    @Mock
    private UnhandledExceptionEventFormer mEventFormer;
    @Mock
    private ReporterLifecycleListener reporterLifecycleListener;
    private CrashReporter mCrashReporter;

    @Override
    protected BaseReporter getReporter() {
        return new CrashReporter(
                mContext,
                mReportsHandler,
                mReporterEnvironment,
                mExtraMetaInfoRetriever,
                mCrashToFileWriter,
                mEventFormer,
                processDetector,
                unhandledExceptionConverter,
                regularErrorConverter,
                customErrorConverter,
                anrConverter,
                pluginErrorDetailsConverter
        );
    }

    @Before
    public void setUp() {
        super.setUp();
        mCrashReporter = (CrashReporter) mReporter;
    }

    @Test
    public void testMainReporterListenerIsCalledInConstructor() {
        when(ClientServiceLocator.getInstance().getReporterLifecycleListener())
                .thenReturn(reporterLifecycleListener);
        final BaseReporter reporter = getReporter();
        verify(reporterLifecycleListener).onCreateCrashReporter(any(CrashReporterContext.class));
    }

    @Test
    public void reportUnhandledException() {
        UnhandledException unhandledException = mock(UnhandledException.class);
        ReportToSend reportToSend = mock(ReportToSend.class);
        when(mEventFormer.formEvent(unhandledException, mReporterEnvironment)).thenReturn(reportToSend);
        mReporter.reportUnhandledException(unhandledException);
        verify(mCrashToFileWriter).writeToFile(reportToSend);
    }

    @Test
    public void reporterType() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey).build();
        mReporter = new CrashReporter(
                mContext,
                mock(ProcessConfiguration.class),
                config,
                mReportsHandler
        );
        assertThat(mReporter.getEnvironment().getReporterConfiguration().getReporterType()).isEqualTo(CounterConfigurationReporterType.CRASH);
    }

    @Test
    public void updateConfigNullErrorEnvironment() {
        mCrashReporter.updateConfig(AppMetricaConfig.newConfigBuilder(apiKey).build());
        verify(mReporterEnvironment, never()).putErrorEnvironmentValue(anyString(), anyString());
    }

    @Test
    public void updateConfigFilledErrorEnvironment() {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        mCrashReporter.updateConfig(
                AppMetricaConfig.newConfigBuilder(apiKey)
                        .withErrorEnvironmentValue(key1, value1)
                        .withErrorEnvironmentValue(key2, value2)
                        .build()
        );
        verify(mReporterEnvironment).putErrorEnvironmentValue(key1, value1);
        verify(mReporterEnvironment).putErrorEnvironmentValue(key2, value2);
    }
}
