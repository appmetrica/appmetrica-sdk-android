package io.appmetrica.analytics.impl.proxy;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreapi.internal.backport.Provider;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.proxy.synchronous.PluginsReporterSynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.PluginsReporterBarrier;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.plugins.PluginErrorDetails;

public class PluginReporterProxy implements IPluginReporter {

    @NonNull
    private final PluginsReporterBarrier barrier;
    @NonNull
    private final PluginsReporterSynchronousStageExecutor synchronousStageExecutor;
    @NonNull
    private final ICommonExecutor executor;
    @NonNull
    private final Provider<IReporterExtended> reporterProvider;

    public PluginReporterProxy(
        @NonNull Provider<IReporterExtended> reporterProvider
    ) {
        this.barrier = new PluginsReporterBarrier();
        this.synchronousStageExecutor = new PluginsReporterSynchronousStageExecutor();
        this.executor = ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor();
        this.reporterProvider = reporterProvider;
    }

    @Override
    public void reportUnhandledException(@NonNull final PluginErrorDetails errorDetails) {
        barrier.reportUnhandledException(errorDetails);
        synchronousStageExecutor.reportPluginUnhandledException(errorDetails);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUnhandledException(errorDetails);
            }
        });
    }

    @Override
    public void reportError(@NonNull final PluginErrorDetails errorDetails, @Nullable final String message) {
        if (!barrier.reportErrorWithFilledStacktrace(errorDetails, message)) {
            Log.w(SdkUtils.APPMETRICA_TAG, "Error stacktrace must be non empty");
            return;
        }
        synchronousStageExecutor.reportPluginError(errorDetails, message);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportError(errorDetails, message);
            }
        });
    }

    @Override
    public void reportError(@NonNull final String identifier,
                            @Nullable final String message,
                            @Nullable final PluginErrorDetails errorDetails) {
        barrier.reportError(identifier, message, errorDetails);
        synchronousStageExecutor.reportPluginError(identifier, message, errorDetails);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportError(identifier, message, errorDetails);
            }
        });
    }

    @WorkerThread
    private IPluginReporter getReporter() {
        return reporterProvider.get().getPluginExtension();
    }
}
