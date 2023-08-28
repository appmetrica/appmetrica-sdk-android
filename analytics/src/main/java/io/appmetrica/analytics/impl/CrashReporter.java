package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.CrashToFileWriter;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.UnhandledExceptionEventFormer;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.reporter.CrashReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.internal.CounterConfiguration;

public class CrashReporter extends BaseReporter {

    private static final String TAG = "[CrashReporter]";

    @NonNull
    private final CrashToFileWriter mCrashToFileWriter;
    @NonNull
    private final UnhandledExceptionEventFormer mEventFormer;

    CrashReporter(@NonNull Context context,
                  @NonNull ProcessConfiguration processConfiguration,
                  @NonNull AppMetricaConfig config,
                  @NonNull ReportsHandler reportsHandler) {
        this(context, processConfiguration, config, reportsHandler, new ExtraMetaInfoRetriever(context));
    }

    private CrashReporter(@NonNull Context context,
                          @NonNull ProcessConfiguration processConfiguration,
                          @NonNull AppMetricaConfig config,
                          @NonNull ReportsHandler reportsHandler,
                          @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        this(
                context,
                reportsHandler,
                new ReporterEnvironment(
                        processConfiguration,
                        new CounterConfiguration(config, CounterConfigurationReporterType.CRASH),
                        config.userProfileID
                ),
                extraMetaInfoRetriever,
                new CrashToFileWriter(context),
                new UnhandledExceptionEventFormer(),
                ClientServiceLocator.getInstance().getProcessDetector(),
                new UnhandledExceptionConverter(),
                new RegularErrorConverter(),
                new CustomErrorConverter(),
                new AnrConverter(),
                new PluginErrorDetailsConverter(extraMetaInfoRetriever)
        );
    }

    @VisibleForTesting
    CrashReporter(@NonNull Context context,
                  @NonNull ReportsHandler reportsHandler,
                  @NonNull ReporterEnvironment reporterEnvironment,
                  @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever,
                  @NonNull CrashToFileWriter crashToFileWriter,
                  @NonNull UnhandledExceptionEventFormer eventFormer,
                  @NonNull ProcessDetector processDetector,
                  @NonNull UnhandledExceptionConverter unhandledExceptionConverter,
                  @NonNull RegularErrorConverter regularErrorConverter,
                  @NonNull CustomErrorConverter customErrorConverter,
                  @NonNull AnrConverter anrConverter,
                  @NonNull PluginErrorDetailsConverter pluginErrorDetailsConverter) {
        super(
                context,
                reportsHandler,
                reporterEnvironment,
                extraMetaInfoRetriever,
                processDetector,
                unhandledExceptionConverter,
                regularErrorConverter,
                customErrorConverter,
                anrConverter,
                pluginErrorDetailsConverter
        );
        mCrashToFileWriter = crashToFileWriter;
        mEventFormer = eventFormer;
        final ReporterLifecycleListener listener =
                ClientServiceLocator.getInstance().getReporterLifecycleListener();
        if (listener != null) {
            final CrashReporterContext crashReporterContext = new CrashReporterContext();
            listener.onCreateCrashReporter(crashReporterContext);
        }
    }

    @Override
    public void reportUnhandledException(@NonNull UnhandledException unhandledException) {
        YLogger.debug(TAG, "reportUnhandledException: %s", unhandledException.exception);
        mCrashToFileWriter.writeToFile(mEventFormer.formEvent(unhandledException, mReporterEnvironment));
        logUnhandledException(unhandledException);
    }

    public void updateConfig(@NonNull AppMetricaConfig config) {
        putAllToErrorEnvironment(config.errorEnvironment);
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
