package io.appmetrica.analytics.impl.preparer;

import android.text.TextUtils;
import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;

public class ProtobufNativeCrashComposer implements ValueComposer, EncodingTypeProvider {

    @Override
    public int getEncodingType(@NonNull EventEncryptionMode encryptionMode) {
        return EventProto.ReportMessage.Session.Event.GZIP;
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        if (TextUtils.isEmpty(event.getValue())) {
            return new byte[0];
        } else {
            return Base64.decode(event.getValue(), Base64.DEFAULT);
        }
    }
}
