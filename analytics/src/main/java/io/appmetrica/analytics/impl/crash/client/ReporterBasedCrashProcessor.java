package io.appmetrica.analytics.impl.crash.client;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ICrashTransformer;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.UnhandledSituationReporterProvider;

public class ReporterBasedCrashProcessor extends CrashProcessor {

    @NonNull
    private UnhandledSituationReporterProvider mReporterProvider;

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
        mReporterProvider.getReporter().reportUnhandledException(unhandledException);
    }

    @VisibleForTesting
    @NonNull
    public UnhandledSituationReporterProvider getReporterProvider() {
        return mReporterProvider;
    }

}
