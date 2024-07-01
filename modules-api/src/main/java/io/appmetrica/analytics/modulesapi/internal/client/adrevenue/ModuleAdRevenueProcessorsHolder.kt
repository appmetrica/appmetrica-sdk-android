package io.appmetrica.analytics.modulesapi.internal.client.adrevenue

interface ModuleAdRevenueProcessorsHolder {

    fun register(processor: ModuleAdRevenueProcessor)
}
