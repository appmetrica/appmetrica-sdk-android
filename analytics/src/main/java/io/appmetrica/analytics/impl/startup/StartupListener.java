package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface StartupListener {

    void onStartupChanged(@NonNull StartupState newState);

    void onStartupError(@NonNull StartupError error, @Nullable StartupState existingState);

}
