package io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo;

import androidx.annotation.NonNull;

public interface ChargeTypeChangeListener {

    void onChargeTypeChanged(@NonNull ChargeType chargeType);

}
