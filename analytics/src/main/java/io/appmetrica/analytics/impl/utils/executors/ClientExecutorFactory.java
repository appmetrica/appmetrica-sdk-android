package io.appmetrica.analytics.impl.utils.executors;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

public class ClientExecutorFactory {

    @NonNull
    public Handler createMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @NonNull
    public ExecutorWrapper createReportsSenderExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.CLIENT_REPORTS_SENDER_THREAD);
    }

    @NonNull
    public ExecutorWrapper createDefaultExecutor() {
        return new ExecutorWrapper(NamedThreadFactory.CLIENT_DEFAULT_THREAD);
    }

    @NonNull
    public Thread createInitCoreThread(@NonNull Runnable runnable) {
        return NamedThreadFactory.newThread(NamedThreadFactory.CLIENT_INIT_CORE_THREAD, runnable);
    }
}
