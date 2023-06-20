package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import androidx.annotation.Nullable;

public class RuntimeConfig {

    @Nullable
    public final String errorEnvironment;
    @Nullable
    public final String handlerVersion;

    public RuntimeConfig(@Nullable String errorEnvironment, @Nullable String handlerVersion) {
        this.errorEnvironment = errorEnvironment;
        this.handlerVersion = handlerVersion;
    }

    @Override
    public String toString() {
        return "RuntimeConfig{" +
                "errorEnvironment='" + errorEnvironment + '\'' +
                ", handlerVersion='" + handlerVersion + '\'' +
                '}';
    }
}
