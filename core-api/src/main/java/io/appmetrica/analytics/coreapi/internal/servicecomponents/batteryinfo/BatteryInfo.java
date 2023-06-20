package io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BatteryInfo {

    @Nullable
    public final Integer batteryLevel;
    @NonNull
    public final ChargeType chargeType;

    public BatteryInfo(@Nullable Integer batteryLevel, @NonNull ChargeType chargeType) {
        this.batteryLevel = batteryLevel;
        this.chargeType = chargeType;
    }
}
