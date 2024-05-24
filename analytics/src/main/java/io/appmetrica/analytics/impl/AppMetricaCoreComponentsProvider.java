package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.stub.AppMetricaCoreStub;
import io.appmetrica.analytics.impl.stub.AppMetricaImplStub;
import io.appmetrica.analytics.impl.utils.UnlockedUserStateProvider;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;

public class AppMetricaCoreComponentsProvider {

    @NonNull
    private final UnlockedUserStateProvider unlockedUserStateProvider;

    @Nullable
    private volatile Boolean useStubs;
    @Nullable
    private IAppMetricaCore appMetricaCore;
    @Nullable
    private IAppMetricaImpl appMetricaImpl;

    public AppMetricaCoreComponentsProvider() {
        this(new UnlockedUserStateProvider());
    }

    public synchronized IAppMetricaCore getCore(@NonNull Context context,
                                                @NonNull ClientExecutorProvider clientExecutorProvider) {
        if (appMetricaCore == null) {
            if (shouldUseStubs(context)) {
                appMetricaCore = new AppMetricaCoreStub();
            } else {
                appMetricaCore = new AppMetricaCore(context, clientExecutorProvider);
            }
        }

        return appMetricaCore;
    }

    public synchronized IAppMetricaImpl getImpl(@NonNull Context context,
                                                @NonNull IAppMetricaCore appMetricaCore) {
        if (appMetricaImpl == null) {
            if (shouldUseStubs(context)) {
                appMetricaImpl = new AppMetricaImplStub();
            } else {
                appMetricaImpl = new AppMetricaImpl(context, appMetricaCore);
            }
        }

        return appMetricaImpl;
    }

    public boolean shouldUseStubs(@NonNull Context context) {
        Boolean localCopy = useStubs;
        if (localCopy == null) {
            synchronized (this) {
                localCopy = useStubs;
                if (localCopy == null) {
                    localCopy = !unlockedUserStateProvider.isUserUnlocked(context);
                    useStubs = localCopy;
                    if (localCopy) {
                        SdkUtils.logStubUsage();
                    }
                }
            }
        }
        return localCopy;
    }

    public boolean peekShouldUseStubs() {
        return Boolean.TRUE.equals(useStubs);
    }

    @VisibleForTesting
    AppMetricaCoreComponentsProvider(@NonNull UnlockedUserStateProvider unlockedUserStateProvider) {
        this.unlockedUserStateProvider = unlockedUserStateProvider;
    }

}
