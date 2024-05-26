package io.appmetrica.analytics.impl.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import androidx.annotation.NonNull;

/**
 * Main process detection strategy from <a href="https://cs.chromium.org/chromium/src/base/android/java/src/org/chromium/base/ContextUtils.java?q=ContextUtil&sq=package:chromium&g=0&l=172">Chromium</a>.
 */
public class MainProcessDetector implements ProcessDetector {

    private volatile String mProcessName;

    /** @return The name of the current process. E.g. "org.chromium.chrome:privileged_process0". */
    @Override
    public String getProcessName() {
        // Once we drop support JB, this method can be simplified to not cache sProcessName and call
        // ActivityThread.currentProcessName().
        if (mProcessName != null) {
            return mProcessName;
        }
        // Before JB MR2, currentActivityThread() returns null when called on a non-UI thread.
        // Cache the name to allow other threads to access it.

        synchronized (this) {
            if (mProcessName == null) {
                mProcessName = extractProcessFromActivityThread();
            }
        }

        return mProcessName;
    }

    @SuppressLint("PrivateApi")
    private String extractProcessFromActivityThread() {
        try {
            // An even more convenient ActivityThread.currentProcessName() exists, but was not added
            // until JB MR2.
            Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
            Object activityThread =
                    activityThreadClazz.getMethod("currentActivityThread").invoke(null);
            // Before JB MR2, currentActivityThread() returns null when called on a non-UI thread.
            // Cache the name to allow other threads to access it.
            return (String) activityThreadClazz.getMethod("getProcessName").invoke(activityThread);
        } catch (Throwable e) { // No multi-catch below API level 19 for reflection exceptions.
            // If fallback logic is ever needed, refer to:
            // https://chromium-review.googlesource.com/c/chromium/src/+/905563/1
            throw new RuntimeException(e);
        }
    }

    public boolean isMainProcess() {
        try {
            return !TextUtils.isEmpty(getProcessName()) && !getProcessName().contains(":");
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean isNonMainProcess(@NonNull String privateProcessName) {
        try {
            return !TextUtils.isEmpty(getProcessName())
                    && getProcessName().endsWith(":" + privateProcessName);
        } catch (Throwable e) {
            return false;
        }
    }

}
