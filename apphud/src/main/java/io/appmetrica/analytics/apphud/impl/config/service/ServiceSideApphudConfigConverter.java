package io.appmetrica.analytics.apphud.impl.config.service;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
import io.appmetrica.analytics.apphud.impl.protobuf.client.RemoteApphudConfigProtobuf;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.io.IOException;

public class ServiceSideApphudConfigConverter implements Converter<ServiceSideApphudConfig, byte[]> {

    @Override
    public byte[] fromModel(@NonNull ServiceSideApphudConfig value) {
        RemoteApphudConfigProtobuf.RemoteApphudConfig proto = new RemoteApphudConfigProtobuf.RemoteApphudConfig();
        proto.enabled = value.isEnabled();
        if (value.getApiKey() != null) {
            proto.apiKey = value.getApiKey();
        }
        return MessageNano.toByteArray(proto);
    }

    @NonNull
    @Override
    public ServiceSideApphudConfig toModel(@NonNull byte[] value) {
        try {
            RemoteApphudConfigProtobuf.RemoteApphudConfig proto =
                RemoteApphudConfigProtobuf.RemoteApphudConfig.parseFrom(value);
            return new ServiceSideApphudConfig(
                proto.enabled,
                proto.apiKey
            );
        } catch (IOException e) {
            RemoteApphudConfigProtobuf.RemoteApphudConfig proto =
                new RemoteApphudConfigProtobuf.RemoteApphudConfig();
            return new ServiceSideApphudConfig(
                proto.enabled,
                proto.apiKey
            );
        }
    }
}
