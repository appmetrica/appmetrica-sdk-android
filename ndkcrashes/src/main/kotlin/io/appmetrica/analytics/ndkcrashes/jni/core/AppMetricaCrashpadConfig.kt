package io.appmetrica.analytics.ndkcrashes.jni.core

internal class AppMetricaCrashpadConfig(
    val handlerPath: String,
    val crashFolder: String,
    val socketName: String,
    val appMetricaMetadata: String,
    val is64bit: Boolean = false,
    val javaHandlerClassName: String = "",
    val apkPath: String = "",
    val dataDir: String = "",
    val libPath: String = ""
) {
    override fun toString(): String {
        return "AppMetricaCrashpadConfig(" +
            "handlerPath=$handlerPath, " +
            "crashFolder=$crashFolder, " +
            "socketName=$socketName, " +
            "appMetricaMetadata=$appMetricaMetadata, " +
            "is64bit=$is64bit, " +
            "javaHandlerClassName=$javaHandlerClassName, " +
            "apkPath=$apkPath, " +
            "dataDir=$dataDir, " +
            "libPath=$libPath, " +
            ")"
    }
}
