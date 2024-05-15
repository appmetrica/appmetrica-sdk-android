package io.appmetrica.analytics.impl.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Main process detection strategy from <a href="https://cs.chromium.org/chromium/src/base/android/java/src/org/chromium/base/ContextUtils.java?q=ContextUtil&sq=package:chromium&g=0&l=172">Chromium</a>.
 */
public class MainProcessDetector implements ProcessDetector {

    private static final String TAG = "[MainProcessDetector]";

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
                mProcessName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ?
                        extractProcessNameSinceJBMR2() : extractProcessNameBeforeJBMR2();
            }
        }

        return mProcessName;
    }

    @SuppressLint("StaticFieldLeak")
    private String extractProcessNameBeforeJBMR2() {
        String result = null;
        try {
            FutureTask<String> future = new FutureTask<String>(new Callable<String>() {
                @Override
                public String call() {
                    return extractProcessFromActivityThread();
                }
            });
            ClientServiceLocator.getInstance().getClientExecutorProvider().getMainHandler().post(future);
            result = future.get(5, TimeUnit.SECONDS);
        } catch (Throwable e) {
            DebugLogger.error(TAG, e, e.getMessage());
        }

        return result;
    }

    private String extractProcessNameSinceJBMR2() {
        return extractProcessFromActivityThread();
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
            return TextUtils.isEmpty(getProcessName()) == false && !getProcessName().contains(":");
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean isNonMainProcess(@NonNull String privateProcessName) {
        try {
            return TextUtils.isEmpty(getProcessName()) == false
                    && getProcessName().endsWith(":" + privateProcessName);
        } catch (Throwable e) {
            return false;
        }
    }

}
