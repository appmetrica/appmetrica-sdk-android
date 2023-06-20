package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import java.util.Map;

public class ClientIdentifiersProvider {

    @NonNull
    private final StartupUnit mStartupUnit;
    @NonNull
    private final AdvertisingIdGetter mAdvertisingIdGetter;
    @NonNull
    private final Context mContext;

    public ClientIdentifiersProvider(@NonNull StartupUnit startupUnit,
                                     @NonNull AdvertisingIdGetter advertisingIdGetter,
                                     @NonNull Context context) {
        mStartupUnit = startupUnit;
        mAdvertisingIdGetter = advertisingIdGetter;
        mContext = context;
    }

    public ClientIdentifiersHolder createClientIdentifiersHolder(@Nullable Map<String, String> clientClids) {
        return new ClientIdentifiersHolder(
                mStartupUnit.getStartupState(),
                mAdvertisingIdGetter.getIdentifiersForced(mContext),
                clientClids
        );
    }
}
