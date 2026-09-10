package io.appmetrica.analytics.impl.preparer;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.component.processor.event.ReportSaveInitHandler;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class ValueWithPreloadInfoComposer implements ValueComposer {

    private static final String TAG = "[ValueWithPreloadInfoComposer]";

    @NonNull
    private final StringValueComposer mStringValueComposer;

    public ValueWithPreloadInfoComposer() {
        this(new StringValueComposer());
    }

    @VisibleForTesting
    ValueWithPreloadInfoComposer(@NonNull StringValueComposer composer) {
        mStringValueComposer = composer;
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        String valueString = asUtf8String(event);
        DebugLogger.INSTANCE.info(TAG, "compose value from %s", valueString);
        if (config.needToSendPreloadInfo() == false) {
            DebugLogger.INSTANCE.info(TAG, "removing preload info");
            if (TextUtils.isEmpty(valueString) == false) {
                try {
                    JSONObject valueJson = new JSONObject(valueString);
                    valueJson.remove(ReportSaveInitHandler.JsonKeys.PRELOAD_INFO);
                    event.updateValue(valueJson.toString());
                } catch (Throwable ex) {
                    DebugLogger.INSTANCE.error(TAG, ex);
                }
            }
        }
        return mStringValueComposer.getValue(event, config);
    }

    private static String asUtf8String(@NonNull EventFromDbModel event) {
        return event.foldValue(
            legacy -> legacy,
            bytes -> new String(bytes, StandardCharsets.UTF_8),
            () -> null
        );
    }
}
