package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.startup.StartupUnit;

public class ClientIdentifiersProviderFactory {

    @NonNull
    public ClientIdentifiersProvider createClientIdentifiersProvider(@NonNull StartupUnit startupUnit,
                                                                     @NonNull AdvertisingIdGetter advertisingIdGetter,
                                                                     @NonNull Context context) {
        return new ClientIdentifiersProvider(startupUnit, advertisingIdGetter, context);
    }
}
