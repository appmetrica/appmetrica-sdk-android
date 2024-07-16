package io.appmetrica.analytics.modulesapi.internal.client.adrevenue

interface ModuleAdRevenueProcessor {

    /**
     * Processes AdRevenue values from AppMetrica with modules.
     * Returns true if processed successfully and false otherwise.
     */
    fun process(vararg values: Any?): Boolean

    /**
     * Returns the description of the processor
     */
    fun getDescription(): String
}
