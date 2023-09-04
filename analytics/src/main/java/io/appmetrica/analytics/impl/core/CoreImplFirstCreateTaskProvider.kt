package io.appmetrica.analytics.impl.core

class CoreImplFirstCreateTaskProvider {

    fun tasks(): List<Runnable> = listOf(
        ReportKotlinVersionTask()
    )
}
