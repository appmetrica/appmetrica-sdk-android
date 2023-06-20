package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.PermissionsCollectingConfig;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import org.json.JSONObject;

public class PermissionsCollectingConfigParser {

    void parseIfEnabled(@NonNull StartupResult result, @NonNull JsonHelper.OptJSONObject response) {
        if (result.getCollectionFlags().permissionsCollectingEnabled) {
            JSONObject permissionsConfig = response.optJSONObject(JsonResponseKey.PERMISSIONS_COLLECTING_CONFIG);
            final long checkIntervalSeconds;
            final long forceSendIntervalSeconds;
            final StartupStateProtobuf.StartupState.PermissionsCollectingConfig defaultConfig =
                    new StartupStateProtobuf.StartupState.PermissionsCollectingConfig();
            if (permissionsConfig != null) {
                checkIntervalSeconds = permissionsConfig.optLong(
                        JsonResponseKey.CHECK_INTERVAL_SECONDS,
                        defaultConfig.checkIntervalSeconds
                );
                forceSendIntervalSeconds = permissionsConfig.optLong(
                        JsonResponseKey.FORCE_SEND_INTERVAL_SECONDS,
                        defaultConfig.forceSendIntervalSeconds
                );
            } else {
                checkIntervalSeconds = defaultConfig.checkIntervalSeconds;
                forceSendIntervalSeconds = defaultConfig.forceSendIntervalSeconds;
            }
            result.setPermissionsCollectingConfig(
                    new PermissionsCollectingConfig(checkIntervalSeconds, forceSendIntervalSeconds)
            );
        }
    }
}
