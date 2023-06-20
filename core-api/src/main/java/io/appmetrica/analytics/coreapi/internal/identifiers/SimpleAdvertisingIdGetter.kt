package io.appmetrica.analytics.coreapi.internal.identifiers

import android.content.Context

interface SimpleAdvertisingIdGetter {

    fun getIdentifiers(context: Context): AdvertisingIdsHolder
}
