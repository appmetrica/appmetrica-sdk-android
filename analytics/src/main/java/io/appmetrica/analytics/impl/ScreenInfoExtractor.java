package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ScreenInfoExtractor {

    private static final String TAG = "[ScreenInfoExtractor]";

    @Nullable
    public ScreenInfo extractScreenInfo(@NonNull Context context) {
        Point size = extractScreenSize(context);
        if (size == null) {
            return null;
        }
        int width = Math.max(size.x, size.y);
        int height = Math.min(size.x, size.y);
        int dpi = 0;
        float scaleFactor = 0;
        try {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            dpi = displayMetrics.densityDpi;
            scaleFactor = displayMetrics.density;
        } catch (Throwable ex) {
            DebugLogger.INSTANCE.error(TAG, ex);
        }
        return new ScreenInfo(width, height, dpi, scaleFactor);
    }

    @SuppressLint("NewApi")
    @Nullable
    private Point extractScreenSize(@NonNull final Context context) {
        Point result = null;
        try {
            Display display = null;
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)) {
                try {
                    display = context.getDisplay();
                } catch (Throwable ex) {
                    DebugLogger.INSTANCE.error(TAG, ex);
                }
            }
            if (display == null) {
                final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                display = windowManager.getDefaultDisplay();
            }
            if (display != null) {
                result = extractScreenSizeFromDisplay(display);
            }
        } catch (Throwable ex) {
            DebugLogger.INSTANCE.error(TAG, ex);
        }
        return result;
    }

    // Method is based on this SO answer: http://stackoverflow.com/a/23861333
    @AnyThread
    @Nullable
    private Point extractScreenSizeFromDisplay(@NonNull Display display) {
        return SystemServiceUtils.accessSystemServiceSafely(
            display,
            "getting display metrics",
            "Display",
            new FunctionWithThrowable<Display, Point>() {
                @Override
                public Point apply(@NonNull Display input) {
                    // New pleasant way to get real metrics
                    DisplayMetrics realMetrics = new DisplayMetrics();
                    input.getRealMetrics(realMetrics);
                    return new Point(realMetrics.widthPixels, realMetrics.heightPixels);
                }
            }
        );
    }
}
