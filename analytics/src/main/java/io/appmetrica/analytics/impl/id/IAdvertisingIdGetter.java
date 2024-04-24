package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter;
import io.appmetrica.analytics.impl.StartupStateObserver;
import io.appmetrica.analytics.impl.startup.StartupState;

public interface IAdvertisingIdGetter extends SimpleAdvertisingIdGetter, StartupStateObserver {

    void init(@NonNull Context context);

    void init(@NonNull final Context context, @Nullable StartupState startupState);

    @NonNull
    AdvertisingIdsHolder getIdentifiersForced(@NonNull final Context context);

    @NonNull
    AdvertisingIdsHolder getIdentifiersForced(@NonNull final Context context,
                                              @NonNull final RetryStrategy yandexRetryStrategy);

}
