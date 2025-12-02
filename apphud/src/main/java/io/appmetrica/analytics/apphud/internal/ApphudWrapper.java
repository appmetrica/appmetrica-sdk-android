package io.appmetrica.analytics.apphud.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ApphudWrapper {

    void start(
        @NonNull Context context,
        @NonNull String apiKey,
        @Nullable String uuid,
        @Nullable String deviceId,
        boolean observerMode
    );
}
