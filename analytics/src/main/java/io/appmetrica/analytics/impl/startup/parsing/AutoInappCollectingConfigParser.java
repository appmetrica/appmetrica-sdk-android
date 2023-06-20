package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.db.state.converter.AutoInappCollectingConfigConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import org.json.JSONObject;

class AutoInappCollectingConfigParser {

    @NonNull
    private final AutoInappCollectingConfigConverter autoInappCollectingConfigConverter;

    public AutoInappCollectingConfigParser () {
        this(new AutoInappCollectingConfigConverter());
    }

    void parse(@NonNull StartupResult result, @NonNull JSONObject rootJson) {
        result.setAutoInappCollectingConfig(
                autoInappCollectingConfigConverter.toModel(parseAutoInappCollectingConfigToProto(rootJson))
        );
    }

    @NonNull
    private StartupStateProtobuf.StartupState.AutoInappCollectingConfig parseAutoInappCollectingConfigToProto(
            @NonNull JSONObject rootJson
    ) {
        StartupStateProtobuf.StartupState.AutoInappCollectingConfig proto =
                new StartupStateProtobuf.StartupState.AutoInappCollectingConfig();

        JSONObject jsonConfig = rootJson.optJSONObject(JsonResponseKey.AUTO_INAPP_COLLECTING);

        if (jsonConfig != null) {
            proto.sendFrequencySeconds = jsonConfig.optInt(
                    JsonResponseKey.SEND_FREQUENCY_SECONDS,
                    proto.sendFrequencySeconds
            );

            proto.firstCollectingInappMaxAgeSeconds = jsonConfig.optInt(
                    JsonResponseKey.FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS,
                    proto.firstCollectingInappMaxAgeSeconds
            );
        }
        return proto;
    }

    @VisibleForTesting
    AutoInappCollectingConfigParser(
            @NonNull AutoInappCollectingConfigConverter autoInappCollectingConfigConverter
    ) {
        this.autoInappCollectingConfigConverter = autoInappCollectingConfigConverter;
    }
}
