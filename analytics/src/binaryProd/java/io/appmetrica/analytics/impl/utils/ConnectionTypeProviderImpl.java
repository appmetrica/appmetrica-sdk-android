package io.appmetrica.analytics.impl.utils;

import android.content.Context;

import io.appmetrica.analytics.impl.PhoneUtils;

import androidx.annotation.NonNull;

public class ConnectionTypeProviderImpl implements IConnectionTypeProvider {

    @Override
    public PhoneUtils.NetworkType getConnectionType(@NonNull Context context) {
        return PhoneUtils.getConnectionType(context);
    }

}
