package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;

class ExtractorResult {

    @NonNull
    public final String pathToHandler;
    public final boolean is64bit;
    public final boolean useLinker;
    @Nullable
    public final AppProcessConfig appProcessConfig;

    @SuppressLint("NewApi")
    ExtractorResult(@NonNull String pathToHandler, boolean useLinker, @Nullable AppProcessConfig appProcessConfig) {
        this(pathToHandler, useLinker, appProcessConfig,
                AndroidUtils.isApiAchieved(Build.VERSION_CODES.M) && Process.is64Bit()
        );
    }

    @VisibleForTesting
    ExtractorResult(@NonNull String pathToHandler, boolean useLinker,
                    @Nullable AppProcessConfig appProcessConfig, boolean is64bit
    ) {
        this.pathToHandler = pathToHandler;
        this.useLinker = useLinker;
        this.appProcessConfig = appProcessConfig;
        this.is64bit = is64bit;
    }
}
