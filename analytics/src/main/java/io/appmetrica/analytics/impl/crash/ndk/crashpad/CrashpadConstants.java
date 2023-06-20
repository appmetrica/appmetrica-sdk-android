package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.content.Context;
import androidx.annotation.NonNull;

public class CrashpadConstants {

    public static final String ARGUMENT_CLIENT_DESCRIPTION = "arg_cd";
    public static final String ARGUMENT_RUNTIME_CONFIG = "arg_rc";
    public static final String APPMETRICA_NATIVE_CRASHES_FOLDER = "appmetrica_native_crashes";

    public static String getCrashpadNewCrashSocketName(@NonNull Context context) {
        return context.getPackageName() + "-crashpad_new_crash_socket";
    }
}
