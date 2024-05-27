package io.appmetrica.analytics.impl.preparer;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.component.processor.event.ReportSaveInitHandler;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class ValueWithPreloadInfoComposer implements ValueComposer {

    private static final String TAG = "[ValueWithPreloadInfoComposer]";

    @NonNull
    private final EncryptedStringValueComposer mEncryptedStringValueComposer;

    public ValueWithPreloadInfoComposer() {
        this(new EncryptedStringValueComposer());
    }

    @VisibleForTesting
    ValueWithPreloadInfoComposer(@NonNull EncryptedStringValueComposer composer) {
        mEncryptedStringValueComposer = composer;
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        DebugLogger.INSTANCE.info(TAG, "compose value from %s", event.getValue());
        if (config.needToSendPreloadInfo() == false) {
            DebugLogger.INSTANCE.info(TAG, "removing preload info");
            if (TextUtils.isEmpty(event.getValue()) == false) {
                try {
                    JSONObject valueJson = new JSONObject(event.getValue());
                    valueJson.remove(ReportSaveInitHandler.JsonKeys.PRELOAD_INFO);
                    event.updateValue(valueJson.toString());
                } catch (Throwable ex) {
                    DebugLogger.INSTANCE.error(TAG, ex);
                }
            }
        }
        return mEncryptedStringValueComposer.getValue(event, config);
    }
}
