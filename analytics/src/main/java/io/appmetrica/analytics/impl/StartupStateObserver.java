package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.startup.StartupState;

public interface StartupStateObserver {

    void onStartupStateChanged(@NonNull StartupState startupState);

}
