package io.appmetrica.analytics.ndkcrashes.jni.service

internal class CrashpadCrash(
    val uuid: String,
    val dumpFile: String,
    val creationTime: Long,
    val appMetricaData: String,
)
