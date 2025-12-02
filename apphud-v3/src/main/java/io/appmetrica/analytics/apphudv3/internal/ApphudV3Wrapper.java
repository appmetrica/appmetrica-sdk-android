package io.appmetrica.analytics.apphudv3.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.apphud.sdk.Apphud;
import io.appmetrica.analytics.apphud.internal.ApphudWrapper;
import io.appmetrica.analytics.apphudv3.impl.DummyApphudRuleCallback;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public class ApphudV3Wrapper implements ApphudWrapper {

    private static final String TAG = "[ApphudV3Wrapper]";

    @Override
    public void start(
        @NonNull Context context,
        @NonNull String apiKey,
        @Nullable String uuid,
        @Nullable String deviceId,
        boolean observerMode
    ) {
        try {
            PublicLogger.getAnonymousInstance().info("Activating Apphud v3");
            Apphud.INSTANCE.start(
                context,
                apiKey,
                uuid,
                deviceId,
                observerMode,
                new DummyApphudRuleCallback(),
                apphudUser -> null
            );
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
            PublicLogger.getAnonymousInstance().error("Your version of Apphud is incompatible with AppMetrica");
        }
    }
}
