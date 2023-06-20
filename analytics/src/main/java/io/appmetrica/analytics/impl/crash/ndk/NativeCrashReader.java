package io.appmetrica.analytics.impl.crash.ndk;

import androidx.annotation.NonNull;

public interface NativeCrashReader<Crash> {

     void checkForPreviousSessionCrashes();

     void handleRealtimeCrash(@NonNull Crash crash);

}
