package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.utils.executors.NamedThreadFactory
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import java.util.concurrent.Executor

internal class ExecutorProviderImpl : ExecutorProvider {

    private val serviceExecutorProvider: ServiceExecutorProvider =
        GlobalServiceLocator.getInstance().serviceExecutorProvider

    override fun getDefaultExecutor(): IHandlerExecutor = serviceExecutorProvider.defaultExecutor

    override fun getSupportIOExecutor(): IHandlerExecutor = serviceExecutorProvider.supportIOExecutor

    override val moduleExecutor: IHandlerExecutor = serviceExecutorProvider.moduleExecutor

    override fun getUiExecutor(): Executor = serviceExecutorProvider.uiExecutor

    override fun getReportRunnableExecutor(): Executor = serviceExecutorProvider.reportRunnableExecutor

    override fun getInterruptionThread(
        moduleIdentifier: String,
        threadNamePostfix: String,
        runnable: Runnable
    ): InterruptionSafeThread =
        NamedThreadFactory.newThread("$moduleIdentifier-$threadNamePostfix", runnable)
}
