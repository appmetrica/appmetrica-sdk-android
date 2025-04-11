package io.appmetrica.analytics.impl.crash.jvm.client;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ICrashTransformer;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.UnhandledSituationReporterProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ReporterBasedCrashProcessor extends CrashProcessor {

    private static final String TAG = "[ReporterBasedCrashProcessor]";

    @NonNull
    private final UnhandledSituationReporterProvider mReporterProvider;

    public ReporterBasedCrashProcessor(@NonNull Context context,
                                       @NonNull final UnhandledSituationReporterProvider reporterProvider,
                                       @NonNull Rule rule,
                                       @Nullable ICrashTransformer customCrashTransformer) {
        this(reporterProvider, rule, customCrashTransformer, new ExtraMetaInfoRetriever(context));
    }

    @VisibleForTesting
    ReporterBasedCrashProcessor(@NonNull final UnhandledSituationReporterProvider reporterProvider,
                                @NonNull Rule rule,
                                @Nullable ICrashTransformer customCrashTransformer,
                                @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        super(rule, customCrashTransformer, extraMetaInfoRetriever);
        mReporterProvider = reporterProvider;
    }

    @Override
    void sendCrash(@NonNull UnhandledException unhandledException) {
        DebugLogger.INSTANCE.info(TAG, "sendCrash");
        mReporterProvider.getReporter().reportUnhandledException(unhandledException);
    }
}
