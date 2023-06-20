package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;

public class CrashpadCrash {

    @NonNull
    public final CrashpadCrashReport crashReport;
    @NonNull
    public final ClientDescription clientDescription;
    @NonNull
    public final RuntimeConfig runtimeConfig;

    public CrashpadCrash(@NonNull CrashpadCrashReport crashReport,
                  @NonNull ClientDescription clientDescription,
                  @NonNull RuntimeConfig runtimeConfig) {
        this.crashReport = crashReport;
        this.clientDescription = clientDescription;
        this.runtimeConfig = runtimeConfig;
    }

}
