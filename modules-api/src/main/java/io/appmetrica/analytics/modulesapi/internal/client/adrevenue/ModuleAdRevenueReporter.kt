package io.appmetrica.analytics.modulesapi.internal.client.adrevenue

/**
 * Allows reporting AdRevenue from modules to AppMetrica.
 * Is used for auto AdRevenue collection.
 */
interface ModuleAdRevenueReporter {

    fun reportAutoAdRevenue(adRevenue: ModuleAdRevenue)
}
