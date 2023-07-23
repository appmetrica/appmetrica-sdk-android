package io.appmetrica.analytics.ndkcrashes.jni.core

import io.appmetrica.analytics.ndkcrashes.impl.NativeCrashLogger

internal object NativeCrashCoreJni {
    external fun startHandlerWithLinkerAtCrash(config: AppMetricaCrashpadConfig): Boolean
    external fun startJavaHandlerAtCrash(config: AppMetricaCrashpadConfig): Boolean
    external fun startHandlerAtCrash(config: AppMetricaCrashpadConfig): Boolean
    external fun updateAppMetricaMetadataJni(metadata: String)
}

// for tests
internal object NativeCrashCoreJniWrapper {
    private const val tag = "[NativeCrashCoreJni]"

    @JvmStatic
    fun startHandlerWithLinkerAtCrash(
        handlerPath: String,
        crashFolder: String,
        socketName: String,
        is64bit: Boolean,
        appMetricaMetadata: String
    ): Boolean = NativeCrashCoreJni.startHandlerWithLinkerAtCrash(
        AppMetricaCrashpadConfig(
            handlerPath = handlerPath,
            crashFolder = crashFolder,
            socketName = socketName,
            appMetricaMetadata = appMetricaMetadata,
            is64bit = is64bit
        ).also(::logConfig)
    )

    @JvmStatic
    fun startJavaHandlerAtCrash(
        javaHandlerClassName: String,
        handlerPath: String,
        crashFolder: String,
        socketName: String,
        apkPath: String,
        libPath: String,
        dataDir: String,
        appMetricaMetadata: String
    ): Boolean = NativeCrashCoreJni.startJavaHandlerAtCrash(
        AppMetricaCrashpadConfig(
            handlerPath = handlerPath,
            crashFolder = crashFolder,
            socketName = socketName,
            appMetricaMetadata = appMetricaMetadata,
            javaHandlerClassName = javaHandlerClassName,
            apkPath = apkPath,
            dataDir = dataDir,
            libPath = libPath
        ).also(::logConfig)
    )

    @JvmStatic
    fun startHandlerAtCrash(
        handlerPath: String,
        crashFolder: String,
        socketName: String,
        appMetricaMetadata: String
    ): Boolean = NativeCrashCoreJni.startHandlerAtCrash(
        AppMetricaCrashpadConfig(
            handlerPath = handlerPath,
            crashFolder = crashFolder,
            socketName = socketName,
            appMetricaMetadata = appMetricaMetadata
        ).also(::logConfig)
    )

    @JvmStatic
    fun updateAppMetricaMetadata(metadata: String) {
        NativeCrashCoreJni.updateAppMetricaMetadataJni(metadata)
    }

    private fun logConfig(config: AppMetricaCrashpadConfig) {
        NativeCrashLogger.debug(tag, "Start crashpad handler with config: $config")
    }
}
