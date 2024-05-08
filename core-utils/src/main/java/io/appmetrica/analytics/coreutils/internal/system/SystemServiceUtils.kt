package io.appmetrica.analytics.coreutils.internal.system

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.logger.internal.YLogger

object SystemServiceUtils {

    private const val TAG = "[SystemServiceUtils]"

    @JvmStatic
    fun <T, S> accessSystemServiceSafely(
        systemService: T?,
        whileWhat: String,
        whatIsNull: String,
        tryBlock: FunctionWithThrowable<T, S?>
    ): S? {
        if (systemService != null) {
            try {
                return tryBlock.apply(systemService)
            } catch (ex: Throwable) {
                YLogger.error(TAG, ex, "Exception while $whileWhat")
            }
        } else {
            YLogger.warning(TAG, "$whatIsNull is null.")
        }
        return null
    }

    @JvmStatic
    fun <T, S> accessSystemServiceSafelyOrDefault(
        systemService: T?,
        whileWhat: String,
        whatIsNull: String,
        defaultValue: S,
        tryBlock: FunctionWithThrowable<T, S?>
    ): S {
        val res = accessSystemServiceSafely(systemService, whileWhat, whatIsNull, tryBlock)
        return res ?: defaultValue
    }

    @JvmStatic
    fun <T, S> accessSystemServiceByNameSafely(
        context: Context,
        serviceName: String,
        whileWhat: String,
        whatIsNull: String,
        tryBlock: FunctionWithThrowable<T, S?>
    ): S? {
        try {
            @Suppress("UNCHECKED_CAST")
            return accessSystemServiceSafely(
                context.getSystemService(serviceName) as T,
                whileWhat,
                whatIsNull,
                tryBlock
            )
        } catch (ex: Throwable) {
            YLogger.error(TAG, ex)
        }
        return null
    }

    @JvmStatic
    fun <T, S> accessSystemServiceByNameSafelyOrDefault(
        context: Context,
        serviceName: String,
        whileWhat: String,
        whatIsNull: String,
        defaultValue: S,
        tryBlock: FunctionWithThrowable<T, S?>
    ): S {
        try {
            @Suppress("UNCHECKED_CAST")
            return accessSystemServiceSafelyOrDefault(
                context.getSystemService(serviceName) as T,
                whileWhat,
                whatIsNull,
                defaultValue,
                tryBlock
            )
        } catch (ex: Throwable) {
            YLogger.error(TAG, ex)
        }
        return defaultValue
    }
}
