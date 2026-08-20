package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.component.clients.ClientRepository
import io.appmetrica.analytics.impl.crash.jvm.service.CrashEventConsumer
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReportConsumer(
    private val context: Context,
    private val clientRepository: ClientRepository
) : CrashEventConsumer {

    private val tag = "[ReportConsumer]"

    private val tasksExecutor: ICommonExecutor =
        GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor

    fun consumeReport(serviceEvent: ServiceEvent, extras: Bundle?) {
        DebugLogger.info(
            tag,
            "serviceEvent: type = ${serviceEvent.type}; " +
                "customType = ${serviceEvent.customType}; name = ${serviceEvent.name}"
        )
        if (!serviceEvent.isUndefinedType) {
            tasksExecutor.execute(
                ReportRunnable(context, serviceEvent, extras, clientRepository)
            )
        } else {
            DebugLogger.warning(tag, "Undefined report type: ${serviceEvent.type}")
        }
    }

    override fun consumeCrash(
        clientDescription: ClientDescription,
        serviceEvent: ServiceEvent,
        commonArguments: CommonArguments
    ) {
        DebugLogger.info(
            tag,
            "consumeCrash with type: ${serviceEvent.type} and name: ${serviceEvent.name}: $clientDescription"
        )
        val unit = clientRepository.getOrCreateClient(clientDescription, commonArguments)
        unit.handle(serviceEvent, commonArguments)
        clientRepository.remove(
            clientDescription.packageName,
            clientDescription.processID,
            clientDescription.processSessionID
        )
    }
}
