package io.appmetrica.analytics.coreutils.internal.reflection

import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[ReflectionUtils]"

object ReflectionUtils {

    @JvmStatic
    inline fun <reified T> loadAndInstantiateClassWithDefaultConstructor(className: String): T? {
        return loadAndInstantiateClassWithDefaultConstructor(className, T::class.java)
    }

    @JvmStatic
    fun <T> loadAndInstantiateClassWithDefaultConstructor(className: String, targetType: Class<T>): T? = try {
        YLogger.info(TAG, "Load and instantiate class `$className` with default constructor")
        loadClass(className, targetType)?.getConstructor()?.newInstance()
    } catch (e: Throwable) {
        YLogger.error(
            TAG,
            e,
            "Failed to instantiate class with name \"%s\" and targetType: %",
            className,
            targetType
        )
        null
    }

    @JvmStatic
    fun <T> loadClass(className: String, targetType: Class<T>): Class<T>? {
        try {
            val clazz = Class.forName(className)
            if (targetType.isAssignableFrom(clazz)) {
                @Suppress("UNCHECKED_CAST")
                return clazz as Class<T>
            }
            YLogger.error(TAG, "Loaded class = %s for name %s couldn't be cast to %s", clazz, className, targetType)
        } catch (throwable: Throwable) {
            YLogger.error(
                TAG, throwable, "Failed to load class for name = %s and targetType = %s", className, targetType
            )
        }
        return null
    }

    @JvmStatic
    fun findClass(className: String): Class<*>? = try {
        Class.forName(className, false, ReflectionUtils::class.java.classLoader)
    } catch (ignored: Throwable) {
        null
    }

    @JvmStatic
    fun detectClassExists(className: String): Boolean = findClass(className) != null
}
