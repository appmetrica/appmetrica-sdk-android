package io.appmetrica.analytics.coreapi.internal.model

data class SdkEnvironment(
    val appVersionInfo: AppVersionInfo,
    val appFramework: String,
    val screenInfo: ScreenInfo,
    val sdkInfo: SdkInfo,
    val deviceType: String,
    val locales: List<String>,
)
