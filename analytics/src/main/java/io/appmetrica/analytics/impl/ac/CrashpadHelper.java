package io.appmetrica.analytics.impl.ac;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class CrashpadHelper {

    public static native void setUpNativeUncaughtExceptionHandler(@NonNull Bundle bundle);

    public static native void updateRuntimeConfig(@NonNull String config);

    public static native void cancelSetUpNativeUncaughtExceptionHandler();

    public static native void logsEnabled(boolean enabled);

    public static native String getLibraryVersion();

    @Nullable
    public static native String getLibDirInsideApk();

}
