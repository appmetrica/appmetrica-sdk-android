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

    @VisibleForTesting
    ClientExecutorProvider(@NonNull ClientExecutorFactory threadFactory) {
        mThreadFactory = threadFactory;
    }

    @NonNull
    @VisibleForTesting
    ClientExecutorFactory getThreadFactory() {
        return mThreadFactory;
    }
}
