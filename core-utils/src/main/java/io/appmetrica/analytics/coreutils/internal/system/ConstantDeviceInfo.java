package io.appmetrica.analytics.coreutils.internal.system;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstantDeviceInfo {

    @NonNull
    public static final String APP_PLATFORM = "android";

    private static final Object sLock = new Object();
    @SuppressLint("StaticFieldLeak")
    private static volatile ConstantDeviceInfo sInstance;

    @NonNull public final String appPlatform;
    public final String manufacturer;
    public final String model;
    public final String osVersion;
    public final int osApiLevel;
    @NonNull public final String deviceRootStatus;
    @NonNull public final List<String> deviceRootStatusMarkers;

    public static ConstantDeviceInfo getInstance() {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new ConstantDeviceInfo();
                }
            }
        }
        return sInstance;
    }

    @VisibleForTesting
    public ConstantDeviceInfo() {
        appPlatform = APP_PLATFORM;

        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        osVersion = Build.VERSION.RELEASE;
        osApiLevel = Build.VERSION.SDK_INT;

        deviceRootStatus = String.valueOf(RootChecker.isRootedPhone());
        deviceRootStatusMarkers = Collections.unmodifiableList(new ArrayList<String>() {
            {
                if (RootChecker.isSuperuserApkExists()) {
                    add("Superuser.apk");
                }
                if (RootChecker.isSuperuserNativeLibExists()) {
                    add("su.so");
                }
            }
        });
    }
}
