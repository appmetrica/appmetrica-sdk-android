package io.appmetrica.analytics.ndkcrashes

import android.annotation.SuppressLint
import android.util.Log
import dalvik.system.PathClassLoader
import io.appmetrica.analytics.ndkcrashes.impl.NativeCrashLogger
import io.appmetrica.analytics.ndkcrashes.impl.utils.AndroidUtils
import io.appmetrica.analytics.ndkcrashes.jni.runner.NativeCrashHandlerRunnerJni

object JavaHandlerRunner {
    private const val TAG = "[HandlerRunner]"
    private const val TRAMPOLINE_LIBRARY = "appmetrica-native-runner"

    @JvmStatic
    fun main(args: Array<String>) {
        if (AndroidUtils.isAndroidNAchieved()) {
            loadLibraryViaSystemClassloader(TRAMPOLINE_LIBRARY)
        } else {
            loadViaFallbackClassloader(TRAMPOLINE_LIBRARY)
        }
        NativeCrashLogger.debug(TAG, "run native crash handler from java runner")
        NativeCrashHandlerRunnerJni.runHandler(args)
    }

    private fun loadLibraryViaSystemClassloader(name: String) {
        try {
            System.loadLibrary(name)
        } catch (t: Throwable) {
            Log.e(TAG, "can't load library $name", t)
            loadViaFallbackClassloader(name)
        }
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private fun loadViaFallbackClassloader(name: String) {
        try {
            // There is a quirk in the implementation of the system classloader in Android M.
            // The caller of this process (see crashpad_android.cc) sets the CLASSPATH and
            // LD_LIBRARY_PATH to the list of APKs and the list of native libs dirs + list of native
            // libs dirs within the APK. These env vars are then used to initialize system properties
            // java.class.path and java.library.path respectively. These properties should be used to
            // set up system classloader. However, Android M ignores non-directory entries in
            // java.library.path (see https://nda.ya.ru/t/_Hy9h-eJ3iTW3H).

            // To work around this we set up our own classloader passing libraryPath in such a way that
            // APK entries aren't ignored. The we use the classloader to locate the library.
            val classpath = System.getProperty("java.class.path")
            val nativeLibsPath = System.getProperty("java.library.path")
            val fallbackClassLoader = PathClassLoader(classpath, nativeLibsPath, ClassLoader.getSystemClassLoader())
            val libraryPath = fallbackClassLoader.findLibrary(name)
            if (libraryPath != null) {
                System.load(libraryPath)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "can't load library via fallback $name", t)
        }
    }
}
