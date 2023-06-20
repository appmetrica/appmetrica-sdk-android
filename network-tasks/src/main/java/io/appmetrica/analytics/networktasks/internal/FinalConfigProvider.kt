package io.appmetrica.analytics.networktasks.internal

class FinalConfigProvider<T : Any>(
    private val cachedConfig: T
) : ConfigProvider<T> {

    override fun getConfig(): T = cachedConfig
}
