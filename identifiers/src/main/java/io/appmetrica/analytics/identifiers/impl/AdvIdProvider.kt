package io.appmetrica.analytics.identifiers.impl

import android.content.Context

internal interface AdvIdProvider {
    fun getAdTrackingInfo(context: Context): AdvIdResult
}
