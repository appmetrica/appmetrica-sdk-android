package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import java.util.ArrayList;
import java.util.List;

public class LifecycleDependentComponentManager {

    @NonNull
    private final BatteryChargeTypeListener batteryChargeTypeListener;
    @NonNull
    private final ApplicationStateProviderImpl applicationStateProvider;

    @NonNull
    private final List<ServiceLifecycleObserver> serviceLifecycleObservers = new ArrayList<>();

    public LifecycleDependentComponentManager(@NonNull Context context, @NonNull IHandlerExecutor defaultExecutor) {
        this(
            new BatteryChargeTypeListener(context, defaultExecutor),
            new ApplicationStateProviderImpl()
        );
    }

    @VisibleForTesting
    LifecycleDependentComponentManager(@NonNull BatteryChargeTypeListener batteryChargeTypeListener,
                                       @NonNull ApplicationStateProviderImpl applicationStateProvider) {
        this.batteryChargeTypeListener = batteryChargeTypeListener;
        serviceLifecycleObservers.add(batteryChargeTypeListener);
        this.applicationStateProvider = applicationStateProvider;
        serviceLifecycleObservers.add(applicationStateProvider);
    }

    public synchronized void onCreate() {
        for (ServiceLifecycleObserver serviceLifecycleObserver : serviceLifecycleObservers) {
            serviceLifecycleObserver.onCreate();
        }
    }

    public synchronized void onDestroy() {
        for (ServiceLifecycleObserver serviceLifecycleObserver : serviceLifecycleObservers) {
            serviceLifecycleObserver.onDestroy();
        }
    }

    public synchronized void addLifecycleObserver(@NonNull ServiceLifecycleObserver serviceLifecycleObserver) {
        serviceLifecycleObservers.add(serviceLifecycleObserver);
    }

    @NonNull
    public BatteryChargeTypeListener getBatteryChargeTypeListener() {
        return batteryChargeTypeListener;
    }

    @NonNull
    public ApplicationStateProviderImpl getApplicationStateProvider() {
        return applicationStateProvider;
    }
}
