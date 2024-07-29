package io.appmetrica.analytics.impl.crash.jvm.client;

import androidx.annotation.Nullable;

public class RegularError {

    @Nullable
    public final String message;
    @Nullable
    public final UnhandledException exception;

    public RegularError(@Nullable String message, @Nullable UnhandledException exception) {
        this.message = message;
        this.exception = exception;
    }
}
