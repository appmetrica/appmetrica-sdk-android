package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo

interface ScreenInfoProvider {

    val screenInfo: ScreenInfo
}
