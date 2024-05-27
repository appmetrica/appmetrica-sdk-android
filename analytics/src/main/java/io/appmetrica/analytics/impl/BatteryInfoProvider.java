package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.os.BatteryManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.BatteryInfo;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeType;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeChangeListener;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeProvider;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.List;

public class BatteryInfoProvider implements ChargeTypeProvider {

    private static final String TAG = "[BatteryInfoProvider]";

    public static final ChargeType DEFAULT_CHARGE_TYPE = ChargeType.UNKNOWN;

    @NonNull
    private final ICommonExecutor mExecutor;
    @Nullable
    private volatile BatteryInfo mBatteryInfo;

    private final List<ChargeTypeChangeListener> mChargeTypeChangeListeners =
            new ArrayList<ChargeTypeChangeListener>();

    private final Consumer<Intent> batteryChargeTypeRootListener = new Consumer<Intent>() {
        @Override
        public void consume(@Nullable Intent intent) {
            DebugLogger.INSTANCE.info(TAG, "onReceive power state update with intent: %s", intent);
            final BatteryInfo oldBatteryInfo = mBatteryInfo;
            ChargeType oldChargeType = oldBatteryInfo == null ? null : oldBatteryInfo.chargeType;
            final BatteryInfo newBatteryInfo = extractBatteryInfo(intent);
            mBatteryInfo = newBatteryInfo;
            if (oldChargeType != newBatteryInfo.chargeType) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Post update charge type: %s -> %s",
                    oldChargeType == null ? null : oldChargeType.name(),
                    newBatteryInfo.chargeType.name()
                );
                mExecutor.execute(new SafeRunnable() {
                    @Override
                    public void runSafety() throws Exception {
                        notifyChargeTypeChanged(newBatteryInfo.chargeType);
                    }
                });
            }
        }
    };

    public BatteryInfoProvider(@NonNull ICommonExecutor executor,
                               @NonNull BatteryChargeTypeListener batteryChargeTypeListener) {
        mExecutor = executor;
        mBatteryInfo = extractBatteryInfo(batteryChargeTypeListener
                .addStickyBatteryChargeTypeListener(batteryChargeTypeRootListener));
    }

    @Nullable
    public Integer getBatteryLevel() {
        final BatteryInfo batteryInfo = mBatteryInfo;
        return batteryInfo == null ? null : batteryInfo.batteryLevel;
    }

    @Override
    @NonNull
    public ChargeType getChargeType() {
        final BatteryInfo batteryInfo = mBatteryInfo;
        ChargeType chargeType =
                batteryInfo == null ? ChargeType.UNKNOWN : batteryInfo.chargeType;
        DebugLogger.INSTANCE.info(TAG, "Return charge type = %s", chargeType);
        return chargeType;
    }

    @Override
    public synchronized void registerChargeTypeListener(@NonNull ChargeTypeChangeListener listener) {
        mChargeTypeChangeListeners.add(listener);
        DebugLogger.INSTANCE.info(
            TAG,
            "Register charge type listener. Total count: %d",
            mChargeTypeChangeListeners.size()
        );
        listener.onChargeTypeChanged(getChargeType());
    }

    private synchronized void notifyChargeTypeChanged(@NonNull ChargeType chargeType) {
        for (ChargeTypeChangeListener listener : mChargeTypeChangeListeners) {
            listener.onChargeTypeChanged(chargeType);
        }
    }

    @NonNull
    private BatteryInfo extractBatteryInfo(@Nullable Intent batteryStatus) {
        Integer batteryLevel = null;
        ChargeType chargeType = DEFAULT_CHARGE_TYPE;

        if (batteryStatus != null) {
            batteryLevel = extractBatteryLevel(batteryStatus);
            chargeType = extractChargeType(batteryStatus);
        } else {
            DebugLogger.INSTANCE.info(TAG, "Could not get battery status from sticky broadcast");
        }

        return new BatteryInfo(batteryLevel, chargeType);
    }

    @Nullable
    private Integer extractBatteryLevel(@NonNull Intent batteryStatus) {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level > 0 && scale > 0) ? level * 100 / scale : null;
    }

    @NonNull
    private ChargeType extractChargeType(@NonNull Intent batteryStatus) {
        int chargePlugType = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        DebugLogger.INSTANCE.info(TAG, "Extracted chargeType: %d", chargePlugType);
        ChargeType chargeType = ChargeType.NONE;

        switch (chargePlugType) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                chargeType = ChargeType.AC;
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                chargeType = ChargeType.USB;
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                chargeType = ChargeType.WIRELESS;
                break;
        }

        return chargeType;
    }
}
