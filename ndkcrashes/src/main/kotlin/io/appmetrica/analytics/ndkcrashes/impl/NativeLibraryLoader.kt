package io.appmetrica.analytics.ndkcrashes.impl

import androidx.annotation.VisibleForTesting

// use class, mockito can not mock object
class AppMetricaNativeLibraryLoader : NativeLibraryLoader("appmetrica-native")
class AppMetricaServiceNativeLibraryLoader : NativeLibraryLoader("appmetrica-service-native")

abstract class NativeLibraryLoader(private val name: String) {
    @VisibleForTesting
    internal class LibraryLoader {
        fun load(name: String): Unit = System.loadLibrary(name)
    }

    private enum class State {
        BLANK, LOADING_ERROR, LOADED
    }

    private val tag = "[NativeLibraryLoader]"
    private val libraryLoader = LibraryLoader()

    private var state = State.BLANK

    @Synchronized
    fun loadIfNeeded(): Boolean {
        return if (state == State.LOADED) {
            NativeCrashLogger.debug(tag, "native library $name has been already loaded")
            true
        } else {
            if (state == State.LOADING_ERROR) {
                NativeCrashLogger.debug(tag, "there was unsuccessful attempt to load library $name")
                false
            } else {
                try {
                    libraryLoader.load(name)
                    state = State.LOADED
                    NativeCrashLogger.debug(tag, "native library $name is loaded")
                    true
                } catch (e: Throwable) {
                    NativeCrashLogger.error(tag, "can't load native library $name", e)
                    state = State.LOADING_ERROR
                    false
                }
            }
        }
    }
}
