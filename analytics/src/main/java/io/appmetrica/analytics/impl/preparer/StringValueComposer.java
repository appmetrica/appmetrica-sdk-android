package io.appmetrica.analytics.impl.preparer;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class StringValueComposer implements ValueComposer {

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        if (TextUtils.isEmpty(event.getValue()) == false) {
            return StringUtils.getUTF8Bytes(event.getValue());
        }
        return new byte[0];
    }
}
