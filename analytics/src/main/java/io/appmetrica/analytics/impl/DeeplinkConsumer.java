package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import java.util.HashMap;
import java.util.Map;

public class DeeplinkConsumer {

    private static final String TAG = "[DeeplinkConsumer]";

    @NonNull
    private final IMainReporter mainReporter;
    @NonNull
    private final Map<Boolean, String> deeplinksBySource = new HashMap<Boolean, String>();

    public DeeplinkConsumer(@NonNull IMainReporter mainReporter) {
        this.mainReporter = mainReporter;
    }

    @ApiProxyThread
    public void reportAppOpen(@Nullable final Intent intent) {
        if (intent != null) {
            String deeplink = intent.getDataString();
            reportAppOpenInternal(deeplink, false);
        }
    }

    @ApiProxyThread
    public void reportAutoAppOpen(@Nullable final String deeplink) {
        reportAppOpenInternal(deeplink, true);
    }

    @ApiProxyThread
    public void reportAppOpen(@Nullable final String deeplink) {
        reportAppOpenInternal(deeplink, false);
    }

    @ApiProxyThread
    private void reportAppOpenInternal(@Nullable String deeplink, boolean auto) {
        YLogger.info(TAG, "Try to report deeplink: %s, auto: %b", deeplink, auto);
        if (TextUtils.isEmpty(deeplink) == false) {
            if (Utils.areEqual(deeplink, deeplinksBySource.get(!auto)) == false) {
                mainReporter.reportAppOpen(deeplink, auto);
            } else {
                YLogger.info(TAG, "Deeplink %s has already been reported from another source. " +
                        "Skipping.", deeplink);
            }
            deeplinksBySource.put(auto, deeplink);
        }
    }
}
