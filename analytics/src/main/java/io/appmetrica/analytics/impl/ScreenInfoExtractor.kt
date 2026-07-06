package io.appmetrica.analytics.impl

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ScreenInfoExtractor {

    private val tag = "[ScreenInfoExtractor]"

    fun extractScreenInfo(context: Context): ScreenInfo? {
        val size = extractScreenSize(context) ?: return null
        val width = maxOf(size.x, size.y)
        val height = minOf(size.x, size.y)
        var dpi = 0
        var scaleFactor = 0f
        try {
            val metrics = context.resources.displayMetrics
            dpi = metrics.densityDpi
            scaleFactor = metrics.density
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
        }
        return ScreenInfo(width, height, dpi, scaleFactor)
    }

    private fun extractScreenSize(context: Context): Point? = try {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)) {
            extractScreenSizeFromMaximumWindowMetrics(context)
        } else {
            extractScreenSizeBeforeApi30(context)
        }
    } catch (ex: Throwable) {
        DebugLogger.error(tag, ex)
        null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun extractScreenSizeFromMaximumWindowMetrics(context: Context): Point? {
        val windowManager = getWindowManagerForMetrics(context) ?: return null
        return SystemServiceUtils.accessSystemServiceSafely(
            windowManager,
            "getting maximum window metrics",
            "WindowManager"
        ) { windowManagerService ->
            val bounds = windowManagerService.maximumWindowMetrics.bounds
            Point(bounds.width(), bounds.height())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getWindowManagerForMetrics(context: Context): WindowManager? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (context is Activity || windowManager == null) {
            return windowManager
        }
        return try {
            val windowContext = if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.S)) {
                context.createWindowContext(context.display, WindowManager.LayoutParams.TYPE_APPLICATION, null)
            } else {
                context.createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION, null)
            }
            windowContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: windowManager
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
            windowManager
        }
    }

    // Method is based on this SO answer: http://stackoverflow.com/a/23861333
    @Suppress("DEPRECATION")
    private fun extractScreenSizeBeforeApi30(context: Context): Point? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return null
        val display = windowManager.defaultDisplay ?: return null
        return SystemServiceUtils.accessSystemServiceSafely(
            display,
            "getting display metrics",
            "Display"
        ) { displayService ->
            val realMetrics = DisplayMetrics()
            displayService.getRealMetrics(realMetrics)
            Point(realMetrics.widthPixels, realMetrics.heightPixels)
        }
    }
}
