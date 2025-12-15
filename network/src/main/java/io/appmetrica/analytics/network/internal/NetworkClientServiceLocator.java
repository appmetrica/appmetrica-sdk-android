package io.appmetrica.analytics.network.internal;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;

public class NetworkClientServiceLocator {

    @Nullable
    private final Bundle applicationMetaData;

    private static volatile NetworkClientServiceLocator instance;

    @VisibleForTesting
    NetworkClientServiceLocator(@Nullable Bundle applicationMetaData) {
        this.applicationMetaData = applicationMetaData;
    }

    @NonNull
    public static NetworkClientServiceLocator getInstance() {
        return instance;
    }

    @AnyThread
    public static void init(@NonNull Context context) {
        init(context, new SafePackageManager());
    }

    @AnyThread
    public static void init(@NonNull Context context, @NonNull SafePackageManager packageManager) {
        if (instance == null) {
            synchronized (NetworkClientServiceLocator.class) {
                if (instance == null) {
                    instance = new NetworkClientServiceLocator(
                        packageManager.getApplicationMetaData(context)
                    );
                }
            }
        }
    }

    @Nullable
    public Bundle getApplicationMetaData() {
        return applicationMetaData;
    }
}
