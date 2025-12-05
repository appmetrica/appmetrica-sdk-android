package io.appmetrica.analytics.impl.utils.executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ServiceExecutorProvider {

    @NonNull
    private final ServiceExecutorFactory mServiceExecutorFactory;

    @Nullable
    private volatile IHandlerExecutor mMetricaCoreExecutor;
    @Nullable
    private volatile IHandlerExecutor mReportRunnableExecutor;
    @Nullable
    private volatile IHandlerExecutor moduleExecutor;
    @Nullable
    private volatile IHandlerExecutor mNetworkTaskProcessorExecutor;
    @Nullable
    private volatile IHandlerExecutor supportIOExecutor;
    @Nullable
    private volatile IHandlerExecutor mDefaultExecutor;
    @Nullable
    private volatile Executor uiExecutor;

    private Map<String, IHandlerExecutor> customModulesExecutors = new HashMap<>();

    public ServiceExecutorProvider() {
        this(new ServiceExecutorFactory());
    }

    @NonNull
    public IHandlerExecutor getMetricaCoreExecutor() {
        if (mMetricaCoreExecutor  == null) {
            synchronized (this) {
                if (mMetricaCoreExecutor == null) {
                    mMetricaCoreExecutor = mServiceExecutorFactory.createMetricaCoreExecutor();
                }
            }
        }
        return mMetricaCoreExecutor;
    }

    @NonNull
    public IHandlerExecutor getReportRunnableExecutor() {
        if (mReportRunnableExecutor == null) {
            synchronized (this) {
                if (mReportRunnableExecutor == null) {
                    mReportRunnableExecutor = mServiceExecutorFactory.createReportRunnableExecutor();
                }
            }
        }
        return mReportRunnableExecutor;
    }

    @NonNull
    public IHandlerExecutor getModuleExecutor() {
        if (moduleExecutor == null) {
            synchronized (this) {
                if (moduleExecutor == null) {
                    moduleExecutor = mServiceExecutorFactory.createModuleExecutor();
                }
            }
        }
        return moduleExecutor;
    }

    @NonNull
    public IHandlerExecutor getNetworkTaskProcessorExecutor() {
        if (mNetworkTaskProcessorExecutor == null) {
            synchronized (this) {
                if (mNetworkTaskProcessorExecutor == null) {
                    mNetworkTaskProcessorExecutor = mServiceExecutorFactory.createNetworkTaskProcessorExecutor();
                }
            }
        }
        return mNetworkTaskProcessorExecutor;
    }

    @NonNull
    public IHandlerExecutor getSupportIOExecutor() {
        if (supportIOExecutor == null) {
            synchronized (this) {
                if (supportIOExecutor == null) {
                    supportIOExecutor = mServiceExecutorFactory.createSupportIOExecutor();
                }
            }
        }
        return supportIOExecutor;
    }

    @NonNull
    public IHandlerExecutor getDefaultExecutor() {
        if (mDefaultExecutor == null) {
            synchronized (this) {
                if (mDefaultExecutor == null) {
                    mDefaultExecutor = mServiceExecutorFactory.createDefaultExecutor();
                }
            }
        }
        return mDefaultExecutor;
    }

    @NonNull
    public synchronized IHandlerExecutor getCustomModuleExecutor(@NonNull String tag) {
        IHandlerExecutor executor = customModulesExecutors.get(tag);
        if (executor == null) {
            executor = mServiceExecutorFactory.createCustomModuleExecutor(tag);
            customModulesExecutors.put(tag, executor);
        }
        return executor;
    }

    @NonNull
    public Executor getUiExecutor() {
        if (uiExecutor == null) {
            synchronized (this) {
                if (uiExecutor == null) {
                    uiExecutor = mServiceExecutorFactory.createUiExecutor();
                }
            }
        }
        return uiExecutor;
    }

    @NonNull
    public InterruptionSafeThread getHmsReferrerThread(@NonNull Runnable runnable) {
        return mServiceExecutorFactory.createHmsReferrerThread(runnable);
    }

    @VisibleForTesting
    public void destroy() {
        stopRunning(mMetricaCoreExecutor);
        stopRunning(mReportRunnableExecutor);
        stopRunning(moduleExecutor);
        stopRunning(mNetworkTaskProcessorExecutor);
        stopRunning(supportIOExecutor);
        stopRunning(mDefaultExecutor);
    }

    private void stopRunning(@Nullable ICommonExecutor executor) {
        if (executor != null && executor.isRunning()) {
            executor.stopRunning();
        }
    }

    @VisibleForTesting
    @NonNull
    ServiceExecutorFactory getServiceExecutorFactory() {
        return mServiceExecutorFactory;
    }

    @VisibleForTesting
    ServiceExecutorProvider(@NonNull ServiceExecutorFactory serviceExecutorFactory) {
        mServiceExecutorFactory = serviceExecutorFactory;
    }
}
