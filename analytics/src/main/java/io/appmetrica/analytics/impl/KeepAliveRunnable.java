package io.appmetrica.analytics.impl;

import android.os.Handler;
import java.lang.ref.WeakReference;

class KeepAliveRunnable implements Runnable {

    private final WeakReference<Handler> mTaskHandlerRef;
    private final WeakReference<BaseReporter> mReporterRef;

    KeepAliveRunnable(Handler taskHandler, BaseReporter reporter) {
        mTaskHandlerRef = new WeakReference<Handler>(taskHandler);
        mReporterRef = new WeakReference<BaseReporter>(reporter);
    }

    @Override
    public void run() {
        final Handler taskHandler = mTaskHandlerRef.get();
        final BaseReporter reporter = mReporterRef.get();

        if (taskHandler != null && reporter != null) {
            if (reporter.reportKeepAliveIfNeeded()) {
                KeepAliveHandler.scheduleKeepAliveTask(taskHandler, reporter, this);
            }
        }
    }

}
