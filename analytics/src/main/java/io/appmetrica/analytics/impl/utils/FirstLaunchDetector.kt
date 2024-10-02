package io.appmetrica.analytics.impl.utils

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.db.FileConstants
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class FirstLaunchDetector {

    private val tag = "[FirstLaunchDetector]"

    @Volatile
    private var notAFirstLaunch: Boolean? = null

    fun init(context: Context) {
        DebugLogger.info(tag, "init")
        var localValue = notAFirstLaunch
        if (localValue == null) {
            synchronized(this) {
                localValue = notAFirstLaunch
                if (localValue == null) {
                    localValue = detectNotFirstLaunch(context)
                    DebugLogger.info(tag, "detected not a first launch value: $localValue")
                    notAFirstLaunch = localValue
                }
            }
        }
    }

    fun isNotFirstLaunch(): Boolean = notAFirstLaunch == true

    private fun detectNotFirstLaunch(context: Context): Boolean = try {
        val legacyExists = FileUtils.getFileFromAppStorage(context, FileConstants.UUID_FILE_NAME)?.exists() ?: false
        val actualExists = FileUtils.getFileFromSdkStorage(context, FileConstants.UUID_FILE_NAME)?.exists() ?: false
        legacyExists || actualExists
    } catch (e: Throwable) {
        DebugLogger.error(tag, e)
        false
    }
}
