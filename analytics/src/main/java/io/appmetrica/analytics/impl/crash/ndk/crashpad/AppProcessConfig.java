package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import androidx.annotation.NonNull;

public class AppProcessConfig {

    @NonNull
    public final String className = "io.appmetrica.analytics.impl.ac.HandlerRunner";
    @NonNull
    public final String apkPath;
    @NonNull
    public final String libPath;
    @NonNull
    public final String dataDirectory;

    AppProcessConfig(@NonNull String apkPath, @NonNull String libPath, @NonNull String dataDirectory) {
        this.apkPath = apkPath;
        this.libPath = libPath;
        this.dataDirectory = dataDirectory;
    }
}
