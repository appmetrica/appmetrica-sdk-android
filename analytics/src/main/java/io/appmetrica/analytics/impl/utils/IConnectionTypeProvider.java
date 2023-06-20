package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.PhoneUtils;

public interface IConnectionTypeProvider {

    PhoneUtils.NetworkType getConnectionType(@NonNull Context context);
}

