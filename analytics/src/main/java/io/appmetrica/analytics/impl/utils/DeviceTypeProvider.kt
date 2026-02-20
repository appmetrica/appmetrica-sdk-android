package io.appmetrica.analytics.impl.utils

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import kotlin.math.sqrt

internal object DeviceTypeProvider {

    private const val TAG = "[DeviceTypeProvider]"

    private val safePackageManager = SafePackageManager()

    private const val MIN_SW_DP_FOR_TABLET = 600
    private const val DIAGONAL_INCHES_TABLET = 7
    private const val PIXELS_PER_SINGLE_DENSITY_UNIT = 160

    /**
     * Determines if the device is a tablet (tablet, phone?).
     *
     * @param ctx {@link Context} object. The calling context.
     */
    @JvmStatic
    fun getDeviceType(ctx: Context, realDeviceScreenSize: Point): String {
        val density = getDensity(ctx)
        if (density.isNaN() || density == 0.0f) {
            return DeviceTypeValues.PHONE
        }

        val realDeviceWidth = realDeviceScreenSize.x
        val realDeviceHeight = realDeviceScreenSize.y

        if (isAndroidTV(ctx)) {
            return DeviceTypeValues.TV
        }

        val diagonalInchesApprox = getDiagonalInchesApprox(realDeviceHeight, realDeviceWidth, density)
        val smallestWidthDp = getSmallestEdgeSize(realDeviceHeight, realDeviceWidth, density)

        if (diagonalInchesApprox >= DIAGONAL_INCHES_TABLET || smallestWidthDp >= MIN_SW_DP_FOR_TABLET) {
            return DeviceTypeValues.TABLET
        }

        return DeviceTypeValues.PHONE
    }

    private fun getDensity(context: Context): Float {
        return try {
            context.resources.displayMetrics.density
        } catch (ex: Throwable) {
            DebugLogger.error(TAG, ex)
            0.0f
        }
    }

    private fun getSmallestEdgeSize(
        realDeviceHeight: Int,
        realDeviceWidth: Int,
        density: Float
    ): Float {
        val widthDp = realDeviceWidth / density
        val heightDp = realDeviceHeight / density
        return widthDp.coerceAtMost(heightDp)
    }

    private fun getDiagonalInchesApprox(
        realDeviceHeight: Int,
        realDeviceWidth: Int,
        density: Float
    ): Double {
        val densityDpi = density * PIXELS_PER_SINGLE_DENSITY_UNIT
        val widthInchesApprox = realDeviceWidth / densityDpi
        val heightInchesApprox = realDeviceHeight / densityDpi
        return sqrt((widthInchesApprox * widthInchesApprox + heightInchesApprox * heightInchesApprox).toDouble())
    }

    // Based on information from Android Developers:
    // https://developer.android.com/training/tv/get-started/hardware
    private fun isAndroidTV(
        context: Context
    ): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val currentModeType = SystemServiceUtils.accessSystemServiceSafelyOrDefault(
            uiModeManager,
            "getting current mode type",
            "UiModeManager",
            null
        ) { input ->
            input.currentModeType
        }
        val uiModeTypeTelevision = currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val featureLeanback = safePackageManager.hasSystemFeature(context, PackageManager.FEATURE_LEANBACK)
        val featureLeanbackOnly = if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.O)) {
            safePackageManager.hasSystemFeature(context, PackageManager.FEATURE_LEANBACK_ONLY)
        } else {
            false
        }
        return uiModeTypeTelevision ||
            featureLeanback ||
            featureLeanbackOnly
    }
}
