package io.appmetrica.analytics.coreapi.internal.servicecomponents

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor

interface ActivationBarrier {

    fun subscribe(delay: Long, executor: ICommonExecutor, callback: ActivationBarrierCallback)
}
