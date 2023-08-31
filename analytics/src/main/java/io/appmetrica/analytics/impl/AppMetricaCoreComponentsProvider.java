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

    private Boolean useStubs;
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
            if (useStubs(context)) {
                appMetricaCore = new AppMetricaCoreStub(clientExecutorProvider);
            } else {
                appMetricaCore = new AppMetricaCore(context, clientExecutorProvider);
            }
        }

        return appMetricaCore;
    }

    public synchronized IAppMetricaImpl getImpl(@NonNull Context context,
                                                @NonNull IAppMetricaCore appMetricaCore) {
        if (appMetricaImpl == null) {
            if (useStubs(context)) {
                appMetricaImpl = new AppMetricaImplStub();
            } else {
                appMetricaImpl = new AppMetricaImpl(context, appMetricaCore);
            }
        }

        return appMetricaImpl;
    }

    private synchronized boolean useStubs(@NonNull Context context) {
        if (useStubs == null) {
            useStubs = !unlockedUserStateProvider.isUserUnlocked(context);
            if (useStubs) {
                SdkUtils.logStubUsage();
            }
        }
        return useStubs;
    }

    @VisibleForTesting
    AppMetricaCoreComponentsProvider(@NonNull UnlockedUserStateProvider unlockedUserStateProvider) {
        this.unlockedUserStateProvider = unlockedUserStateProvider;
    }

}
