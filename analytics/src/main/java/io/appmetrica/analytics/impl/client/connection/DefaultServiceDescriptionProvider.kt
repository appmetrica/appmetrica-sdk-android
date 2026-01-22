package io.appmetrica.analytics.impl.client.connection

import android.content.Context
import io.appmetrica.analytics.internal.AppMetricaService

internal class DefaultServiceDescriptionProvider : ServiceDescriptionProvider {

    companion object {
        const val SERVICE_SCHEME = "appmetrica"
    }

    override fun serviceDescription(context: Context): ServiceDescription = ServiceDescription(
        context.packageName,
        SERVICE_SCHEME,
        AppMetricaService::class.java
    )
}
