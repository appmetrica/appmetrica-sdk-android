package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.ICrashProcessor;
import io.appmetrica.analytics.impl.crash.utils.CrashedThreadConverter;
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppMetricaUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "[MetricaUncaughtExceptionHandler]";

    private static final AtomicBoolean processDying = new AtomicBoolean();

    @NonNull
    private final List<ICrashProcessor> mCrashProcessors;
    @Nullable
    private final Thread.UncaughtExceptionHandler mDefaultHandler;
    @NonNull
    private final ThreadsStateDumper threadsStateDumper;
    @NonNull
    private final ProcessDetector processDetector;
    @NonNull
    private final CrashedThreadConverter crashedThreadConverter;

    public AppMetricaUncaughtExceptionHandler(@Nullable Thread.UncaughtExceptionHandler defaultHandler,
                                              @NonNull List<ICrashProcessor> crashProcessors) {
        this(
                defaultHandler,
                crashProcessors,
                ClientServiceLocator.getInstance().getProcessDetector(),
                new CrashedThreadConverter(),
                new ThreadsStateDumper()
        );
    }

    @VisibleForTesting
    AppMetricaUncaughtExceptionHandler(Thread.UncaughtExceptionHandler defaultHandler,
                                       @NonNull List<ICrashProcessor> crashProcessors,
                                       @NonNull ProcessDetector processDetector,
                                       @NonNull CrashedThreadConverter crashedThreadConverter,
                                       @NonNull ThreadsStateDumper threadsStateDumper) {
        mCrashProcessors = crashProcessors;
        mDefaultHandler = defaultHandler;
        this.processDetector = processDetector;
        this.crashedThreadConverter = crashedThreadConverter;
        this.threadsStateDumper = threadsStateDumper;
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            DebugLogger.info(TAG, "Process is dying");
            processDying.set(true);
            processUnhandledException(
                    ex,
                    new AllThreads(
                            crashedThreadConverter.apply(thread),
                            threadsStateDumper.getThreadsDumpForCrash(thread),
                            processDetector.getProcessName()
                    )
            );
        } finally {
            if (mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(thread, ex);
            }
        }
    }

    public static boolean isProcessDying() {
        return processDying.get();
    }

    @VisibleForTesting
    void processUnhandledException(@Nullable Throwable originalException,
                                   @NonNull AllThreads allThreads) {
        for (ICrashProcessor crashProcessor : mCrashProcessors) {
            crashProcessor.processCrash(originalException, allThreads);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    List<ICrashProcessor> getCrashProcessors() {
        return mCrashProcessors;
    }
}
