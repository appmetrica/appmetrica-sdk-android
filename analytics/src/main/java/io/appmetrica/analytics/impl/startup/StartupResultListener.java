package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface StartupResultListener {

    void onStartupChanged(@NonNull String packageName, @NonNull StartupState newState);

    void onStartupError(@NonNull String packageName,
                        @NonNull StartupError error,
                        @Nullable StartupState existingState);

}
