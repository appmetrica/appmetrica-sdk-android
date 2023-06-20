package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IModuleReporter {

    void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    );

    void setSessionExtra(@NonNull String key, @Nullable byte[] value);
}
