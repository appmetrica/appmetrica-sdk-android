package io.appmetrica.analytics.impl.crash.ndk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public interface NdkCrashInitializer {

    @NonNull
    String getFolderName();

    @NonNull
    String getLibraryName();

    @WorkerThread
    void setUpHandler(@NonNull String apiKey, @NonNull String folder, @Nullable String errorEnv);

    void cancelSetUp();

    void setLogsEnabled(boolean enabled);

    void updateErrorEnvironment(@Nullable String errorEnvironment);
}
