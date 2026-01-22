package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.impl.component.clients.ClientRepository

internal class AppMetricaServiceCoreImplFieldsFactory {

    fun createReportConsumer(context: Context, clientRepository: ClientRepository): ReportConsumer {
        return ReportConsumer(context, clientRepository)
    }
}
