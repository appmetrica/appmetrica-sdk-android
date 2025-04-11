package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.component.clients.ClientRepository
import io.appmetrica.analytics.impl.crash.jvm.service.CrashEventConsumer
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class ReportConsumer(
    private val context: Context,
    private val clientRepository: ClientRepository
) : CrashEventConsumer {

    private val tag = "[ReportConsumer]"

    private val tasksExecutor: ICommonExecutor =
        GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor

    fun consumeReport(reportData: CounterReport, extras: Bundle?) {
        DebugLogger.info(
            tag,
            "reportData: type = ${reportData.type}; customType = ${reportData.customType}; name = ${reportData.name}"
        )
        if (!reportData.isUndefinedType) {
            tasksExecutor.execute(
                ReportRunnable(context, reportData, extras, clientRepository)
            )
        } else {
            DebugLogger.warning(tag, "Undefined report type: ${reportData.type}")
        }
    }

    override fun consumeCrash(
        clientDescription: ClientDescription,
        counterReport: CounterReport,
        commonArguments: CommonArguments
    ) {
        DebugLogger.info(
            tag,
            "consumeCrash with type: ${counterReport.type} and name: ${counterReport.name}: $clientDescription"
        )
        val unit = clientRepository.getOrCreateClient(clientDescription, commonArguments)
        unit.handle(counterReport, commonArguments)
        clientRepository.remove(
            clientDescription.packageName,
            clientDescription.processID,
            clientDescription.processSessionID
        )
    }
}
