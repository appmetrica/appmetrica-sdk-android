package io.appmetrica.analytics.apphud.impl.config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.protobuf.client.ModuleConfigProtobuf;
import io.appmetrica.analytics.coreapi.internal.data.JsonParser;
import org.json.JSONObject;

public class ModuleConfigParser implements JsonParser<ModuleConfig> {

    @NonNull
    private final ModuleConfigToProtoConverter converter;

    public ModuleConfigParser(@NonNull ModuleConfigToProtoConverter converter) {
        this.converter = converter;
    }

    @NonNull
    @Override
    public ModuleConfig parse(@NonNull JSONObject rawData) {
        ModuleConfigProtobuf.ModuleConfig proto = new ModuleConfigProtobuf.ModuleConfig();
        proto.apiKey = rawData.optString(Constants.Startup.API_KEY_KEY);

        return converter.toModel(proto);
    }

    @Nullable
    @Override
    public ModuleConfig parseOrNull(@NonNull JSONObject rawData) {
        return parse(rawData);
    }
}
