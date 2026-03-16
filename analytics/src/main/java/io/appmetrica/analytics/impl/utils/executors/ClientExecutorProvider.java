package io.appmetrica.analytics.impl.utils.executors;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;

public class ClientExecutorProvider {

    @NonNull
    private final ClientExecutorFactory mThreadFactory;
    @Nullable
    private volatile IHandlerExecutor mDefaultExecutorWrapper;
    @Nullable
    private volatile ICommonExecutor mReportSenderExecutor;
    @Nullable
    private volatile Handler mMainHandler;
    @Nullable
    private volatile IHandlerExecutor mPersistenceExecutor;

    public ClientExecutorProvider() {
        this(new ClientExecutorFactory());
    }

    @NonNull
    public IHandlerExecutor getDefaultExecutor() {
        if (mDefaultExecutorWrapper == null) {
            synchronized (this) {
                if (mDefaultExecutorWrapper == null) {
                    mDefaultExecutorWrapper = mThreadFactory.createDefaultExecutor();
                }
            }
        }
        return mDefaultExecutorWrapper;
    }

    @NonNull
    public ICommonExecutor getReportSenderExecutor() {
        if (mReportSenderExecutor == null) {
            synchronized (this) {
                if (mReportSenderExecutor == null) {
                    mReportSenderExecutor = mThreadFactory.createReportsSenderExecutor();
                }
            }
        }
        return mReportSenderExecutor;
    }

    @NonNull
    public Handler getMainHandler() {
        if (mMainHandler == null) {
            synchronized (this) {
                if (mMainHandler == null) {
                    mMainHandler = mThreadFactory.createMainHandler();
                }
            }
        }
        return mMainHandler;
    }

    @NonNull
    public IHandlerExecutor getPersistenceExecutor() {
        if (mPersistenceExecutor == null) {
            synchronized (this) {
                if (mPersistenceExecutor == null) {
                    mPersistenceExecutor = mThreadFactory.createPersistenceExecutor();
                }
            }
        }
        return mPersistenceExecutor;
    }

    public Thread getCoreInitThread(@NonNull Runnable runnable) {
        return mThreadFactory.createInitCoreThread(runnable);
    }

    @VisibleForTesting
    ClientExecutorProvider(@NonNull ClientExecutorFactory threadFactory) {
        mThreadFactory = threadFactory;
    }
}
