package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.text.TextUtils;
import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.utils.JsonHelper;

class RuntimeConfigDeserializer {

    private static final String TAG = "[RuntimeConfigDeserializer]";

    @NonNull
    RuntimeConfig deserialize(@NonNull String data) {
        try {
            JsonHelper.OptJSONObject crashData = new JsonHelper.OptJSONObject(
                    new String(Base64.decode(data, Base64.DEFAULT))
            );
            return deserializeRuntimeConfig(crashData);
        } catch (Exception e) { // for any typing problems
            YLogger.error(TAG, e, "not able deserialize config");
            return new RuntimeConfig(null, null);
        }
    }

    @NonNull
    private RuntimeConfig deserializeRuntimeConfig(@NonNull JsonHelper.OptJSONObject description) {
        try {
            String env = description.optString(RuntimeConfigStorage.ARGUMENT_ERROR_ENV, "");
            String handlerVersion = description.has(RuntimeConfigStorage.ARGUMENT_HANDLER_VERSION)
                    ? description.getString(RuntimeConfigStorage.ARGUMENT_HANDLER_VERSION)
                    : null;
            return new RuntimeConfig(
                    TextUtils.isEmpty(env) ? null : env,
                    TextUtils.isEmpty(handlerVersion) ? null : handlerVersion
            );
        } catch (Exception e) { // for any typing problems
            YLogger.error(TAG, e, "not able deserialize config");
            return new RuntimeConfig(null, null);
        }
    }
}
