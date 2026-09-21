package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CoreClientEvent;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.ReporterEnvironment;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.jvm.converter.JvmCrashConverter;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class UnhandledExceptionEventFormer {

    private static final String TAG = "[UnhandledExceptionEventFormer]";

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
        DebugLogger.INSTANCE.info(
            TAG,
            "Forming unhandled exception event with environment: %s",
            reporterEnvironment.getErrorEnvironment()
        );
        final CoreClientEvent reportData = CoreClientEvent.unhandledExceptionReportEntry(
                UnhandledException.getErrorName(unhandledException),
                mJvmCrashConverter.fromModel(unhandledException)
        );
        reportData.setEventEnvironment(reporterEnvironment.getErrorEnvironment());
        return ReportToSend.newBuilder(reportData, reporterEnvironment)
            .asCrash(true)
            .build();
    }
}
