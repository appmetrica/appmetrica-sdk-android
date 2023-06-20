package io.appmetrica.analytics.impl.utils.executors;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.coreutils.internal.executors.SynchronizedBlockingExecutor;
import java.util.Locale;
import java.util.concurrent.Executor;

public class ServiceExecutorFactory {

    @NonNull
    ExecutorWrapper createMetricaCoreExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.SERVICE_CORE);
    }

    @NonNull
    Executor createSynchronizedBlockingExecutor() {
        return new SynchronizedBlockingExecutor();
    }

    @NonNull
    ExecutorWrapper createReportRunnableExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.SERVICE_TASKS_EXECUTOR);
    }

    @NonNull
    ExecutorWrapper createModuleExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.SERVICE_MODULE_THREAD);
    }

    @NonNull
    ExecutorWrapper createNetworkTaskProcessorExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.SERVICE_NETWORK_TASK_PROCESSOR_EXECUTOR);
    }

    @NonNull
    ExecutorWrapper createSupportIOExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.SERVICE_SUPPORT_IO_EXECUTOR);
    }

    @NonNull
    ExecutorWrapper createDefaultExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.SERVICE_DEFAULT_EXECUTOR);
    }

    @NonNull
    ExecutorWrapper createCustomModuleExecutor(@NonNull String tag) {
        return new ExecutorWrapper(String.format(Locale.US, NamedThreadFactory.CUSTOM_MODULE_EXECUTOR_PATTERN, tag));
    }

    @NonNull
    Executor createUiExecutor() {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                mainHandler.post(command);
            }
        };
    }

    @NonNull
    InterruptionSafeThread createHmsReferrerThread(@NonNull Runnable runnable) {
        return NamedThreadFactory.newThread(NamedThreadFactory.SERVICE_HMS_REFERRER_THREAD, runnable);
    }
}
