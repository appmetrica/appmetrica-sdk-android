package io.appmetrica.analytics.impl.core

class MetricaCoreImplFirstCreateTaskProvider {

    fun tasks(): List<Runnable> = listOf(
        ReportKotlinVersionTask()
    )
}
