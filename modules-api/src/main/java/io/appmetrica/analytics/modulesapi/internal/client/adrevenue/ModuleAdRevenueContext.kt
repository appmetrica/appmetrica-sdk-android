package io.appmetrica.analytics.modulesapi.internal.client.adrevenue

interface ModuleAdRevenueContext {

    val adRevenueReporter: ModuleAdRevenueReporter
    val adRevenueProcessorsHolder: ModuleAdRevenueProcessorsHolder
}
