package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.DistributionSourceProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class PreloadInfoState implements DistributionSourceProvider {

    private static final String TAG = "[PreloadInfoData]";

    public static final class JsonKeys {
        public static final String PRELOAD_INFO = "preloadInfo";
        private static final String TRACKING_ID = "trackingId";
        private static final String ADDITIONAL_PARAMS = "additionalParams";
        private static final String WAS_SET = "wasSet";
        private static final String AUTO_TRACKING = "autoTracking";
        private static final String SOURCE = "source";
    }

    @Nullable
    public final String trackingId;
    @NonNull
    public final JSONObject additionalParameters;
    public final boolean wasSet;
    public final boolean autoTrackingEnabled;
    @NonNull
    public final DistributionSource source;

    public PreloadInfoState(@Nullable String trackingId,
                            @NonNull JSONObject additionalParameters,
                            boolean wasSet,
                            boolean autoTrackingEnabled,
                            @NonNull DistributionSource source) {
        this.trackingId = trackingId;
        this.additionalParameters = additionalParameters;
        this.wasSet = wasSet;
        this.autoTrackingEnabled = autoTrackingEnabled;
        this.source = source;
    }

    @NonNull
    @Override
    public DistributionSource getSource() {
        return source;
    }

    @NonNull
    public JSONObject toInternalJson() {
        JSONObject preloadInfoJson = new JSONObject();
        try {
            preloadInfoJson.put(JsonKeys.TRACKING_ID, trackingId);
            preloadInfoJson.put(JsonKeys.ADDITIONAL_PARAMS, additionalParameters);
            preloadInfoJson.put(JsonKeys.WAS_SET, wasSet);
            preloadInfoJson.put(JsonKeys.AUTO_TRACKING, autoTrackingEnabled);
            preloadInfoJson.put(JsonKeys.SOURCE, source.getDescription());
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }
        return preloadInfoJson;
    }

    @Nullable
    public JSONObject toEventJson() {
        JSONObject preloadInfoJson = null;
        if (wasSet) {
            preloadInfoJson = new JSONObject();
            try {
                preloadInfoJson.put(JsonKeys.TRACKING_ID, trackingId);
                if (additionalParameters.length() > 0) {
                    preloadInfoJson.put(JsonKeys.ADDITIONAL_PARAMS, additionalParameters);
                }
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(TAG, e);
            }
        }
        return preloadInfoJson;
    }

    @NonNull
    public static PreloadInfoState fromJson(@Nullable JSONObject json) {
        return new PreloadInfoState(
                JsonUtils.optStringOrNull(json, JsonKeys.TRACKING_ID),
                JsonUtils.optJsonObjectOrDefault(json, JsonKeys.ADDITIONAL_PARAMS, new JSONObject()),
                JsonUtils.optBooleanOrDefault(json, JsonKeys.WAS_SET, false),
                JsonUtils.optBooleanOrDefault(json, JsonKeys.AUTO_TRACKING, false),
                DistributionSource.fromString(JsonUtils.optStringOrNull(json, JsonKeys.SOURCE))
        );
    }

    @Override
    public String toString() {
        return "PreloadInfoState{" +
                "trackingId='" + trackingId + '\'' +
                ", additionalParameters=" + additionalParameters +
                ", wasSet=" + wasSet +
                ", autoTrackingEnabled=" + autoTrackingEnabled +
                ", source=" + source +
                '}';
    }
}
