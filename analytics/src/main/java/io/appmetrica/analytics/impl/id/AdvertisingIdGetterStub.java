package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.startup.StartupState;

public class AdvertisingIdGetterStub implements IAdvertisingIdGetter {

    public static final String USER_IN_LOCKED_STATE = "Device user is in locked state";

    private final AdvertisingIdsHolder advertisingIdsHolderStub = new AdvertisingIdsHolder(
                new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, USER_IN_LOCKED_STATE),
                new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, USER_IN_LOCKED_STATE),
                new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, USER_IN_LOCKED_STATE)
                );

    @Override
    public void lazyInit(@NonNull Context context) {
        //Do nothing
    }

    @Override
    public void init(@NonNull Context context) {
        //Do nothing
    }

    @Override
    public void init(@NonNull Context context,
                     @Nullable StartupState startupState) {
        //Do nothing
    }

    @NonNull
    @Override
    public AdvertisingIdsHolder getIdentifiers(@NonNull Context context) {
        return advertisingIdsHolderStub;
    }

    @NonNull
    @Override
    public AdvertisingIdsHolder getIdentifiersForced(@NonNull Context context) {
        return advertisingIdsHolderStub;
    }

    @NonNull
    @Override
    public AdvertisingIdsHolder getIdentifiersForced(@NonNull Context context,
                                                     @NonNull RetryStrategy yandexRetryStrategy) {
        return advertisingIdsHolderStub;
    }

    @Override
    public void onStartupStateChanged(@NonNull StartupState startupState) {
        //Do nothing
    }

}
