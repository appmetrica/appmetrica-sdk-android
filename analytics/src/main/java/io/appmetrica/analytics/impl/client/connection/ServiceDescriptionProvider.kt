package io.appmetrica.analytics.impl.client.connection

import android.content.Context

internal interface ServiceDescriptionProvider {

    fun serviceDescription(context: Context): ServiceDescription
}
