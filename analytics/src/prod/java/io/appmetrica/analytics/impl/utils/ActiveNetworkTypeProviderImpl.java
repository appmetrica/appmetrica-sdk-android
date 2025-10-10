package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider;
import io.appmetrica.analytics.coreapi.internal.system.NetworkType;
import io.appmetrica.analytics.impl.PhoneUtils;

public class ActiveNetworkTypeProviderImpl implements ActiveNetworkTypeProvider {

    @Override
    @NonNull
    public NetworkType getNetworkType(@NonNull Context context) {
        return PhoneUtils.getConnectionType(context);
    }

}
