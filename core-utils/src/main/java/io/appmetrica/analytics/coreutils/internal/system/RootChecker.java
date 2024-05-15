package io.appmetrica.analytics.coreutils.internal.system;

import android.os.Build;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.io.File;

@SuppressWarnings("checkstyle:rawFileCreation")
public final class RootChecker {

    private static final String TAG = "[RootChecker]";

    // Paths for su.
    private final static String [] BINARY_CHECK_PLACES = {
            "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
            "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"
    };

    private static final String SYSTEM_APP_SUPERUSER_APK_LOLLIPOP = "/system/app/Superuser/Superuser.apk";
    private static final String SYSTEM_APP_SUPERUSER_APK = "/system/app/Superuser.apk";

    public static final class RootStatus {
        public static final int ROOT = 1;
        public static final int NOT_ROOT = 0;
    }

    public static boolean isSuperuserApkExists() {
        try {
            final File file;
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
                file = new File(SYSTEM_APP_SUPERUSER_APK_LOLLIPOP);
            } else {
                file = new File(SYSTEM_APP_SUPERUSER_APK);
            }
            if (file.exists()) {
                DebugLogger.info(TAG, "Detected ROOT_STATUS via superuser APK");
                return true;
            }
        } catch (Throwable error) {
            // Do nothing
        }
        return false;
    }

    public static boolean isSuperuserNativeLibExists() {
        // Find su
        for (final String path : BINARY_CHECK_PLACES) {
            try {
                boolean hasRoot = false;
                if (!AndroidUtils.isApiAchieved(Build.VERSION_CODES.S)) {
                    hasRoot = new File(path + "su").exists();
                }
                if (hasRoot) {
                    DebugLogger.info(TAG, "Detected ROOT_STATUS via native lib");
                    return true;
                }
            } catch (Throwable thr) {
                // Do nothing
            }
        }
        return false;
    }

    public static int isRootedPhone() {
        return isSuperuserApkExists() || isSuperuserNativeLibExists() ?
                RootStatus.ROOT : RootStatus.NOT_ROOT;
    }

}
