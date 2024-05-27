package io.appmetrica.analytics.coreutils.internal.system

import android.annotation.SuppressLint
import android.content.Context
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

object SystemPropertiesHelper {

    private const val TAG = "[SystemPropertiesHelper]"

    @JvmStatic
    @SuppressLint("PrivateApi")
    fun readSystemProperty(name: String): String {
        return try {
            val systemProperties =
                Class.forName("android.os.SystemProperties", true, Context::class.java.classLoader)
            val methodGet = systemProperties.getMethod("get", String::class.java)
            methodGet.invoke(systemProperties, name) as? String ?: ""
        } catch (error: Exception) {
            DebugLogger.error(TAG, error, "[SystemPropertiesHelper] Cannot get system property %s", name)
            ""
        }
    }
}
