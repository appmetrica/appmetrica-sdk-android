package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.ReporterEnvironment;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.converter.JvmCrashConverter;
import io.appmetrica.analytics.impl.utils.LoggerStorage;

public class UnhandledExceptionEventFormer {

    @NonNull
    private final JvmCrashConverter mJvmCrashConverter;

    public UnhandledExceptionEventFormer() {
        this(new JvmCrashConverter());
    }

    @VisibleForTesting
    UnhandledExceptionEventFormer(@NonNull JvmCrashConverter jvmCrashConverter) {
        mJvmCrashConverter = jvmCrashConverter;
    }

    public ReportToSend formEvent(@NonNull UnhandledException unhandledException,
                                  @NonNull ReporterEnvironment reporterEnvironment) {
        final ClientCounterReport reportData = EventsManager.unhandledExceptionReportEntry(
                UnhandledException.getErrorName(unhandledException),
                mJvmCrashConverter.fromModel(unhandledException),
                LoggerStorage.getOrCreatePublicLogger(reporterEnvironment.getReporterConfiguration().getApiKey())
        );
        reportData.setEventEnvironment(reporterEnvironment.getErrorEnvironment());
        return ReportToSend.newBuilder(reportData, reporterEnvironment)
            .withTrimmedFields(reportData.getTrimmedFields())
            .asCrash(true)
            .build();
    }
}
