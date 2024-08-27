package io.appmetrica.analytics.apphud.impl.config;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.protobuf.client.ModuleConfigProtobuf;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.io.IOException;

public class ModuleConfigConverter implements Converter<ModuleConfig, byte[]> {

    @NonNull
    private final ModuleConfigToProtoConverter converter;

    public ModuleConfigConverter(@NonNull ModuleConfigToProtoConverter converter) {
        this.converter = converter;
    }

    @Override
    public byte[] fromModel(ModuleConfig value) {
        return MessageNano.toByteArray(converter.fromModel(value));
    }

    @NonNull
    @Override
    public ModuleConfig toModel(byte[] value) {
        try {
            return converter.toModel(ModuleConfigProtobuf.ModuleConfig.parseFrom(value));
        } catch (IOException e) {
            return converter.toModel(new ModuleConfigProtobuf.ModuleConfig());
        }
    }
}
