package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.UnhandledExceptionConverter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class ReporterReportCustomEventEventTypeTests extends ReporterReportCustomEventEventTypeBaseTests {

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
            processNameProvider,
            mock(UnhandledExceptionConverter.class),
            mock(RegularErrorConverter.class),
            mock(CustomErrorConverter.class),
            mock(AnrConverter.class),
            mock(PluginErrorDetailsConverter.class)
        );
    }
}
