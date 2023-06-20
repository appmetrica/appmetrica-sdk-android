package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import io.appmetrica.analytics.impl.PhoneUtils;

public class ConnectionTypeProviderImpl implements IConnectionTypeProvider {

    @Override
    public PhoneUtils.NetworkType getConnectionType(@NonNull Context context) {
        return PhoneUtils.NetworkType.WIFI;
    }

}
