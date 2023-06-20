package io.appmetrica.analytics.impl;

import android.os.Handler;
import android.os.SystemClock;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;

class KeepAliveHandler {

    private static final int KEEP_ALIVE_TO_SESSION_LENGTH_RATIO = 1000 / 2;

    private final Handler mTasksHandler;
    private final BaseReporter mReporter;

    private final KeepAliveRunnable mKeepAliveRunnable;

    KeepAliveHandler(Handler taskHandler, BaseReporter reporter) {
        mTasksHandler = taskHandler;
        mReporter = reporter;
        mKeepAliveRunnable = new KeepAliveRunnable(taskHandler, reporter);
    }

    void onPauseForegroundSession() {
        // While no interacting with UI elements, we shouldn't keep alive state of App.
        cancelKeepAliveTask(mTasksHandler, mReporter, mKeepAliveRunnable);
    }

    void onResumeForegroundSession() {
        // Even if App seems like inactive (but the App is currently used),
        // we still want to keep alive state. We confirm this each half of duration time of session.
        scheduleKeepAliveTask(mTasksHandler, mReporter, mKeepAliveRunnable);
    }

    static void scheduleKeepAliveTask(Handler taskHandler, BaseReporter reporter, Runnable keepAliveTask) {
        cancelKeepAliveTask(taskHandler, reporter, keepAliveTask);
        taskHandler.postAtTime(keepAliveTask, getKeepAliveTaskToken(reporter), getKeepAliveTaskUptimeMillis(reporter));
    }

    private static long getKeepAliveTaskUptimeMillis(final BaseReporter reporter) {
        return SystemClock.uptimeMillis() + getKeepAliveDelayMillis(reporter);
    }

    private static void cancelKeepAliveTask(Handler taskHandler, BaseReporter reporter, Runnable keepAliveTask) {
        taskHandler.removeCallbacks(keepAliveTask, getKeepAliveTaskToken(reporter));
    }

    private static String getKeepAliveTaskToken(BaseReporter reporter) {
        return reporter.getEnvironment().getReporterConfiguration().getApiKey();
    }

    private static int getKeepAliveDelayMillis(BaseReporter reporter) {
        int defaultDelay = WrapUtils.getOrDefault(
                reporter.getEnvironment().getReporterConfiguration().getSessionTimeout(),
                DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS
        ) * KEEP_ALIVE_TO_SESSION_LENGTH_RATIO;
        return defaultDelay;
    }

}
