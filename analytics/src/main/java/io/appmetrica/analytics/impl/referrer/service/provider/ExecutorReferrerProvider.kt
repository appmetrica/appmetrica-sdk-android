package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener

internal class ExecutorReferrerProvider(
    private val delegate: ReferrerProvider,
    private val executor: ICommonExecutor,
) : ReferrerProvider {
    override val referrerName: String get() = delegate.referrerName

    override fun requestReferrer(listener: ReferrerListener) {
        executor.post {
            delegate.requestReferrer { result ->
                executor.execute { listener.onResult(result) }
            }
        }
    }
}
