package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata
import io.appmetrica.analytics.impl.request.StartupRequestConfig

internal class NativeCrashConsumer(
    private val reportConsumer: ReportConsumer,
    private val metadata: AppMetricaNativeCrashMetadata,
    private val reportCreator: NativeCrashReportCreator
) : Consumer<String> {

    override fun consume(input: String) {
        reportConsumer.consumeCrash(
            ClientDescription(
                metadata.apiKey,
                metadata.packageName,
                metadata.processID,
                metadata.processSessionID,
                metadata.reporterType
            ),
            reportCreator.create(input),
            CommonArguments(
                StartupRequestConfig.Arguments(),
                ReporterArguments(),
                null
            )
        )
    }
}
