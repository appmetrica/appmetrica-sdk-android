package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider;
import io.appmetrica.analytics.coreapi.internal.system.NetworkType;

public class ActiveNetworkTypeProviderImpl implements ActiveNetworkTypeProvider {

    @Override
    public NetworkType getNetworkType(@NonNull Context context) {
        return NetworkType.WIFI;
    }

}
