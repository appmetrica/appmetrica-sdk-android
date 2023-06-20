package io.appmetrica.analytics.impl.db.protobuf;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer;
import io.appmetrica.analytics.protobuf.nano.MessageNano;

public abstract class BaseProtobufStateSerializer<T extends MessageNano> implements ProtobufStateSerializer<T> {

    @NonNull
    public byte[] toByteArray(@NonNull T message) {
        return MessageNano.toByteArray(message);
    }

    @NonNull
    public abstract T defaultValue();

}
