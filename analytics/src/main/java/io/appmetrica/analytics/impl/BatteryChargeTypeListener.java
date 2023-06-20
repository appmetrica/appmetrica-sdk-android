package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.BiConsumer;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.utils.BackgroundBroadcastReceiver;
import java.util.ArrayList;
import java.util.List;

public class BatteryChargeTypeListener implements ServiceLifecycleObserver {

    @NonNull
    private final List<Consumer<Intent>> listeners = new ArrayList<Consumer<Intent>>();
    @Nullable
    private Intent batteryChargeTypeLastIntent = null;
    @NonNull
    private final Context context;
    @NonNull
    private final ContextReceiverSafeWrapper contextReceiverSafeWrapper;

    public BatteryChargeTypeListener(@NonNull Context context, @NonNull ICommonExecutor executor) {
        this(context, executor, new ContextReceiverSafeWrapper.Provider());
    }

    @VisibleForTesting
    BatteryChargeTypeListener(@NonNull Context context,
                              @NonNull final ICommonExecutor executor,
                              @NonNull ContextReceiverSafeWrapper.Provider contextReceiverSafeWrapperProvider) {
        this.context = context;
        this.contextReceiverSafeWrapper = contextReceiverSafeWrapperProvider.create(new BackgroundBroadcastReceiver(
                new BiConsumer<Context, Intent>() {
                    @Override
                    public void consume(Context context, Intent intent) {
                        synchronized (BatteryChargeTypeListener.this) {
                            batteryChargeTypeLastIntent = intent;
                            notifyListeners(intent);
                        }
                    }
                },
                executor
        ));
    }

    @Override
    public synchronized void onCreate() {
        batteryChargeTypeLastIntent = registerBatteryChargeTypeReceiver();
        notifyListeners(batteryChargeTypeLastIntent);
    }

    @Override
    public synchronized void onDestroy() {
        batteryChargeTypeLastIntent = null;
        unregisterBatteryChargeTypeReceiver();
        notifyListeners(null);
    }

    @Nullable
    public synchronized Intent addStickyBatteryChargeTypeListener(@NonNull Consumer<Intent> listener) {
        listeners.add(listener);
        return batteryChargeTypeLastIntent;
    }

    @Nullable
    private Intent registerBatteryChargeTypeReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return contextReceiverSafeWrapper.registerReceiver(context, intentFilter);
    }

    private void unregisterBatteryChargeTypeReceiver() {
        batteryChargeTypeLastIntent = null;
        contextReceiverSafeWrapper.unregisterReceiver(context);
    }

    private void notifyListeners(@Nullable Intent intent) {
        for (Consumer<Intent> listener : listeners) {
            listener.consume(intent);
        }
    }

}
