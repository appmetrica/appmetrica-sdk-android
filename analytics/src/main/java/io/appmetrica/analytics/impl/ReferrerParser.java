package io.appmetrica.analytics.impl;

import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.HashMap;
import java.util.Map;

public class ReferrerParser {

    private static final String TAG = "[ReferrerParser]";
    private static final String DEFERRED_DEEPLINK_KEY = "appmetrica_deep_link";

    @NonNull
    public DeferredDeeplinkState parseDeferredDeeplinkState(@Nullable String referrer) {
        DebugLogger.INSTANCE.info(TAG, "parse deeplink from referrer: %s", referrer);
        Map<String, String> parameters = splitQuery(referrer);
        String deeplink = Uri.decode(parameters.get(DEFERRED_DEEPLINK_KEY));
        Map<String, String> deeplinkParameters = null;
        if (TextUtils.isEmpty(deeplink) == false) {
            deeplinkParameters = parseReferrerParameters(deeplink);
        }
        return new DeferredDeeplinkState(deeplink, deeplinkParameters, referrer);
    }

    @NonNull
    private Map<String, String> parseReferrerParameters(@NonNull String raw) {
        Map<String, String> encodedMap = splitQuery(raw);
        HashMap<String, String> newMap = new HashMap<String, String>(encodedMap.size());
        for (Map.Entry<String, String> entry : encodedMap.entrySet()) {
            newMap.put(Uri.decode(entry.getKey()), Uri.decode(entry.getValue()));
        }
        return newMap;

    }

    @NonNull
    private Map<String, String> splitQuery(@Nullable String query) {
        HashMap<String, String> queryPairs = new HashMap<String, String>();
        if (query != null) {
            String queryWithoutPath = removePath(query);
            if (hasQueryParameters(queryWithoutPath)) {
                String[] pairs = queryWithoutPath.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx >= 0) {
                        queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
                    } else {
                        queryPairs.put(pair, "");
                    }
                }
            }
        }
        return queryPairs;
    }

    @NonNull
    private String removePath(@NonNull String query) {
        int index = query.lastIndexOf('?');
        if (index >= 0) {
            return query.substring(index + 1);
        }

        return query;
    }

    private boolean hasQueryParameters(@NonNull String query) {
        return query.contains("=");
    }
}
