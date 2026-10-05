package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;

public class AppMetricaCoreComponentsProvider {

    @Nullable
    private IAppMetricaCore appMetricaCore;
    @Nullable
    private IAppMetricaImpl appMetricaImpl;

    public synchronized IAppMetricaCore getCore(@NonNull Context context,
                                                @NonNull ClientExecutorProvider clientExecutorProvider) {
        if (appMetricaCore == null) {
            appMetricaCore = new AppMetricaCore(context, clientExecutorProvider);
        }

        return appMetricaCore;
    }

    public synchronized IAppMetricaImpl getImpl(@NonNull Context context,
                                                @NonNull IAppMetricaCore appMetricaCore) {
        if (appMetricaImpl == null) {
            appMetricaImpl = new AppMetricaImpl(context, appMetricaCore);
        }

        return appMetricaImpl;
    }

}
