package io.appmetrica.analytics.impl.core

internal class CoreImplFirstCreateTaskProvider {

    fun tasks(): List<Runnable> = listOf(
        ReportKotlinVersionTask()
    )
}
