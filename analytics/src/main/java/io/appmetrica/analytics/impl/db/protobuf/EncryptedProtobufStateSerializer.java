package io.appmetrica.analytics.impl.db.protobuf;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.impl.db.state.EncryptedStateSerializer;
import io.appmetrica.analytics.protobuf.nano.MessageNano;

public class EncryptedProtobufStateSerializer<T extends MessageNano> extends EncryptedStateSerializer<T>
        implements ProtobufStateSerializer<T> {
    public EncryptedProtobufStateSerializer(@NonNull final ProtobufStateSerializer<T> backedSerializer,
                                            @NonNull final AESEncrypter encrypter) {
        super(backedSerializer, encrypter);
    }
}
