package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.network.NetworkTaskFactory;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;
import java.util.concurrent.TimeUnit;

public class ReportingTaskProcessor<C extends ComponentUnit> extends TaskProcessor<C> {

    private static final String TAG = "[ReportingTaskProcessor]";
    private Runnable mFlushRunnable = new Runnable() {
        @Override
        public void run() {
            flushAllTasks();
        }
    };

    private final ICommonExecutor commonTaskExecutor;

    public ReportingTaskProcessor(@NonNull C component,
                                  @NonNull StartupExecutor startupExecutor,
                                  @NonNull ICommonExecutor commonTaskExecutor) {
        super(component, startupExecutor);
        this.commonTaskExecutor = commonTaskExecutor;
    }

    @Override
    public void scheduleFlushTaskNow() {
        commonTaskExecutor.executeDelayed(mFlushRunnable, TimeUnit.SECONDS.toMillis(1));
    }

    @Override
    public void cancelFlushTask() {
        commonTaskExecutor.remove(mFlushRunnable);
    }

    public void restartFlushTask() {
        if (!isShuttingDown()) {
            cancelFlushTask();
            scheduleFlushTask();
        }
    }

    @Override
    public void runTasks() {
        super.runTasks();
        ReportRequestConfig config = getComponent().getFreshReportRequestConfig();
        DebugLogger.INSTANCE.info(
            TAG,
            "Should run report task. ID = %s. Data sending enabled is %b",
            config.getApiKey(),
            config.getCurrentDataSendingState()
        );
        if (config.getCurrentDataSendingState() && Utils.isApiKeyDefined(config.getApiKey())) {
            final NetworkTask reportTask;

            try {
                reportTask = NetworkTaskFactory.createReportTask(getComponent());
            } catch (Throwable exception) {
                DebugLogger.INSTANCE.error(TAG, exception);
                return;
            }

            startTask(reportTask);
        }
    }

    @VisibleForTesting
    void scheduleFlushTask() {
        if (getComponent().getFreshReportRequestConfig().getDispatchPeriod() > 0) {
            final long dispatchTime = TimeUnit.SECONDS.toMillis(
                getComponent().getFreshReportRequestConfig().getDispatchPeriod()
            );
            commonTaskExecutor.executeDelayed(mFlushRunnable, dispatchTime);
        }
    }

    @NonNull
    @VisibleForTesting
    Runnable getFlushRunnable() {
        return mFlushRunnable;
    }
}
