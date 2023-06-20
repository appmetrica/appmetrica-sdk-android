package io.appmetrica.analytics.coreapi.internal.device

data class ScreenInfo(
    val width: Int,
    val height: Int,
    val dpi: Int,
    val scaleFactor: Float,
    val deviceType: String
)
