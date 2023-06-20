package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;

public class NoneEncodingTypeProvider implements EncodingTypeProvider {

    @Override
    public int getEncodingType(@NonNull EventEncryptionMode encryptionMode) {
        return EventProto.ReportMessage.Session.Event.NONE;
    }
}
