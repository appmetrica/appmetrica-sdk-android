package io.appmetrica.analytics.impl.preparer;

import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import java.nio.charset.StandardCharsets;

public class ProtobufNativeCrashComposer implements ValueComposer, EncodingTypeProvider {

    @Override
    public int getEncodingType() {
        return EventProto.ReportMessage.Session.Event.GZIP;
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        // payload is Base64 text both in legacy string and in value_bytes.
        return event.foldValue(
            legacy -> decodeBase64Text(legacy),
            bytes -> decodeBase64Text(new String(bytes, StandardCharsets.UTF_8)),
            () -> new byte[0]
        );
    }

    @NonNull
    private static byte[] decodeBase64Text(@NonNull String text) {
        if (StringUtils.isNullOrEmpty(text)) {
            return new byte[0];
        }
        return Base64.decode(text, Base64.DEFAULT);
    }
}
