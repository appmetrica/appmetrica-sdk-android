package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.impl.crash.CrashpadListener;
import io.appmetrica.analytics.impl.crash.CrashpadListenerImpl;
import io.appmetrica.analytics.impl.crash.CrashpadListenerStub;
import java.util.ArrayList;
import java.util.List;

public class LifecycleDependentComponentManager {

    @NonNull
    private final CrashpadListener crashpadListener;
    @NonNull
    private final BatteryChargeTypeListener batteryChargeTypeListener;
    @NonNull
    private final ApplicationStateProviderImpl applicationStateProvider;

    @NonNull
    private final List<ServiceLifecycleObserver> serviceLifecycleObservers = new ArrayList<>();

    @SuppressLint("NewApi")
    public LifecycleDependentComponentManager(@NonNull Context context, @NonNull ICommonExecutor defaultExecutor) {
        this(
                AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP) ?
                        new CrashpadListenerImpl(context) : new CrashpadListenerStub(),
                new BatteryChargeTypeListener(context, defaultExecutor),
                new ApplicationStateProviderImpl()
        );
    }

    @VisibleForTesting
    LifecycleDependentComponentManager(@NonNull CrashpadListener crashpadListener,
                                       @NonNull BatteryChargeTypeListener batteryChargeTypeListener,
                                       @NonNull ApplicationStateProviderImpl applicationStateProvider) {
        this.crashpadListener = crashpadListener;
        serviceLifecycleObservers.add(crashpadListener);
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
    public CrashpadListener getCrashpadListener() {
        return crashpadListener;
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
