package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Obfuscator {

    private final Map<String, String> mMapping = new HashMap<String, String>();

    public Obfuscator() {
        mMapping.put(UrlParts.FEATURE_GOOGLE_AID, "g");
        mMapping.put(UrlParts.FEATURE_HUAWEI_OAID, "h");
        mMapping.put(UrlParts.FEATURE_SIM_INFO, "si");
        mMapping.put(UrlParts.FEATURES_COLLECTING, "fc");
        mMapping.put(UrlParts.PERMISSIONS_COLLECTING, "pc");
        mMapping.put(UrlParts.RETRY_POLICY, "rp");
        mMapping.put(UrlParts.CACHE_CONTROL, "cc");
        mMapping.put(UrlParts.AUTO_INAPP_COLLECTING, "aic");
        mMapping.put(UrlParts.ATTRIBUTION, "at");
        mMapping.put(UrlParts.STARTUP_UPDATE, "su");
        mMapping.put(UrlParts.FEATURE_SSL_PINNING, "sp");
    }

    @NonNull
    public String obfuscate(@NonNull String initial) {
        if (mMapping.containsKey(initial)) {
            return mMapping.get(initial);
        }
        return initial;
    }

    @VisibleForTesting
    Collection<String> getObfuscationKeys() {
        return mMapping.values();
    }
}
