package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import org.json.JSONObject;

class RuntimeConfigStorage {

    static final String ARGUMENT_ERROR_ENV = "arg_ee";
    static final String ARGUMENT_HANDLER_VERSION = "arg_hv";

    @Nullable
    private String errorEnv;

    @Nullable
    private String handlerVersion;

    public void setErrorEnvironment(@Nullable String errorEnv) {
        this.errorEnv = errorEnv;
    }

    public void setHandlerVersion(@Nullable String handlerVersion) {
        this.handlerVersion = handlerVersion;
    }

    @NonNull
    public String serialize() {
        try {
            JSONObject object = new JSONObject()
                    .put(ARGUMENT_ERROR_ENV, WrapUtils.getOrDefault(errorEnv, ""))
                    .put(ARGUMENT_HANDLER_VERSION, handlerVersion);
            return Base64.encodeToString(object.toString().getBytes(), Base64.DEFAULT);
        } catch (Throwable ignored) {}
        return "";
    }
}
