package io.appmetrica.analytics.apphud.impl.config;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.protobuf.client.ModuleConfigProtobuf;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;

public class ModuleConfigToProtoConverter implements
    ProtobufConverter<ModuleConfig, ModuleConfigProtobuf.ModuleConfig> {

    @NonNull
    @Override
    public ModuleConfigProtobuf.ModuleConfig fromModel(@NonNull ModuleConfig model) {
        ModuleConfigProtobuf.ModuleConfig proto = new ModuleConfigProtobuf.ModuleConfig();
        proto.apiKey = model.getApiKey();
        return proto;
    }

    @NonNull
    @Override
    public ModuleConfig toModel(@NonNull ModuleConfigProtobuf.ModuleConfig proto) {
        return new ModuleConfig(proto.apiKey);
    }
}
