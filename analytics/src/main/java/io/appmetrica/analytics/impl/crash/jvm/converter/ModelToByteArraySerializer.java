package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.protobuf.nano.MessageNano;

public class ModelToByteArraySerializer<T> {

    @NonNull
    private final ProtobufConverter<T, ? extends MessageNano> wrappedConverter;

    ModelToByteArraySerializer(@NonNull ProtobufConverter<T, ? extends MessageNano> wrappedConverter) {
        this.wrappedConverter = wrappedConverter;
    }

    @NonNull
    public byte[] toProto(@NonNull T value) {
        return MessageNano.toByteArray(wrappedConverter.fromModel(value));
    }

}
