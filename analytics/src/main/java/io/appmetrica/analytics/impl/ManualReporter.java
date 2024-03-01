package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.reporter.ManualReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.internal.CounterConfiguration;

class ManualReporter extends BaseReporter {

    private static final String TAG = "[ManualReporter]";

    ManualReporter(Context context,
                   ProcessConfiguration processConfiguration,
                   @NonNull ReporterConfig config,
                   ReportsHandler reportsHandler) {
        this(context, processConfiguration, config, reportsHandler, new ExtraMetaInfoRetriever(context));
    }

    private ManualReporter(@NonNull Context context,
                           @NonNull ProcessConfiguration processConfiguration,
                           @NonNull ReporterConfig config,
                           @NonNull ReportsHandler reportsHandler,
                           @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        this(
                context,
                reportsHandler,
                config,
                new ReporterEnvironment(processConfiguration, new CounterConfiguration(config), config.userProfileID),
                extraMetaInfoRetriever,
                ClientServiceLocator.getInstance().getProcessDetector(),
                new UnhandledExceptionConverter(),
                new RegularErrorConverter(),
                new CustomErrorConverter(),
                new AnrConverter(),
                new PluginErrorDetailsConverter(extraMetaInfoRetriever)
        );
    }

    @VisibleForTesting
    ManualReporter(Context context,
                   ReportsHandler reportsHandler,
                   @NonNull ReporterConfig config,
                   ReporterEnvironment reporterEnvironment,
                   @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever,
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
        final ReporterLifecycleListener listener =
                ClientServiceLocator.getInstance().getReporterLifecycleListener();
        if (listener != null) {
            final ManualReporterContext manualReporterContext = new ManualReporterContext(
                context,
                config,
                reportsHandler
            );
            listener.onCreateManualReporter(config.apiKey, manualReporterContext, this);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
