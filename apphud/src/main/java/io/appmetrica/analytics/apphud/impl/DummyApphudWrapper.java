package io.appmetrica.analytics.apphud.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.internal.ApphudWrapper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public class DummyApphudWrapper implements ApphudWrapper {

    @Override
    public void start(
        @NonNull Context context,
        @NonNull String apiKey,
        @Nullable String uuid,
        @Nullable String deviceId,
        boolean observerMode
    ) {
        PublicLogger.getAnonymousInstance().warning("Your Apphud version is incompatible with AppMetrica");
    }
}
