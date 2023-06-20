package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils;
import io.appmetrica.analytics.impl.db.state.converter.CacheControlConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CacheControl;
import org.json.JSONObject;

public class CacheControlParser {

    @NonNull
    private final CacheControlConverter mConverter;

    public CacheControlParser() {
        this(new CacheControlConverter());
    }

    @NonNull
    public CacheControl parseFromJson(@NonNull JSONObject rootJson) {
        StartupStateProtobuf.StartupState.CacheControl nano = new StartupStateProtobuf.StartupState.CacheControl();

        JSONObject cacheControlJson = rootJson.optJSONObject(JsonResponseKey.CACHE_CONTROL);
        if (cacheControlJson != null) {
            nano.lastKnownLocationTtl = RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(
                cacheControlJson,
                JsonResponseKey.LAST_KNOWN_LOCATION_TTL,
                nano.lastKnownLocationTtl
            );
        }

        return mConverter.toModel(nano);
    }

    @VisibleForTesting
    CacheControlParser(@NonNull CacheControlConverter converter) {
        mConverter = converter;
    }

    @VisibleForTesting
    @NonNull
    CacheControlConverter getConverter() {
        return mConverter;
    }
}
