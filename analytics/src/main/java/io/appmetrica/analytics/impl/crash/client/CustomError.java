package io.appmetrica.analytics.impl.crash.client;

import androidx.annotation.NonNull;

public class CustomError {

    @NonNull
    public final RegularError regularError;
    @NonNull
    public final String identifier;

    public CustomError(@NonNull RegularError regularError, @NonNull String identifier) {
        this.regularError = regularError;
        this.identifier = identifier;
    }
}
