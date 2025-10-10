package io.appmetrica.analytics.coreapi.internal.system

import android.content.Context

interface ActiveNetworkTypeProvider {
    fun getNetworkType(context: Context): NetworkType
}
