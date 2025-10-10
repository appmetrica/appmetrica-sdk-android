package io.appmetrica.analytics.impl.utils;

import android.content.Context;

import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider;
import io.appmetrica.analytics.coreapi.internal.system.NetworkType;
import io.appmetrica.analytics.impl.PhoneUtils;

import androidx.annotation.NonNull;

public class ActiveNetworkTypeProviderImpl implements ActiveNetworkTypeProvider {

    @Override
    public NetworkType getNetworkType(@NonNull Context context) {
        return PhoneUtils.getConnectionType(context);
    }

}
