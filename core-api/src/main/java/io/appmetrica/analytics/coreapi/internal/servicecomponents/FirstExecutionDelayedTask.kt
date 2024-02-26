package io.appmetrica.analytics.coreapi.internal.servicecomponents

interface FirstExecutionDelayedTask {

    fun setInitialDelaySeconds(delay: Long)

    fun tryExecute(launchDelaySeconds: Long): Boolean
}
