package io.appmetrica.analytics.apphud.impl.config.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.coreapi.internal.data.JsonParser;
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils;
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class RemoteApphudConfigParser implements JsonParser<RemoteApphudConfig> {

    private static final String TAG = "[RemoteModuleConfigParser]";

    @NonNull
    @Override
    public RemoteApphudConfig parse(@NonNull JSONObject rawData) {
        DebugLogger.INSTANCE.info(TAG, "Parsing remote module config");
        boolean featureEnabled = RemoteConfigJsonUtils.extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        );
        JSONObject moduleConfig = rawData.optJSONObject(Constants.RemoteConfig.BLOCK_NAME);
        RemoteApphudConfig config = new RemoteApphudConfig(
            featureEnabled,
            JsonUtils.optStringOrNullable(
                moduleConfig,
                Constants.RemoteConfig.API_KEY_KEY,
                Constants.Defaults.DEFAULT_API_KEY
            )
        );
        DebugLogger.INSTANCE.info(TAG, "Remote module config is '" + config + "'");
        return config;
    }

    @Nullable
    @Override
    public RemoteApphudConfig parseOrNull(JSONObject rawData) {
        return parse(rawData);
    }
}
