package io.appmetrica.analytics.apphud.impl.config.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
import io.appmetrica.analytics.coreapi.internal.data.JsonParser;
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils;
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class ServiceSideApphudConfigParser implements JsonParser<ServiceSideApphudConfig> {

    private static final String TAG = "[ServiceSideApphudConfigParser]";

    @NonNull
    @Override
    public ServiceSideApphudConfig parse(@NonNull JSONObject rawData) {
        DebugLogger.INSTANCE.info(TAG, "Parsing remote module config");
        boolean featureEnabled = RemoteConfigJsonUtils.extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        );
        JSONObject moduleConfig = rawData.optJSONObject(Constants.RemoteConfig.BLOCK_NAME);
        ServiceSideApphudConfig config = new ServiceSideApphudConfig(
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
    public ServiceSideApphudConfig parseOrNull(JSONObject rawData) {
        return parse(rawData);
    }
}
