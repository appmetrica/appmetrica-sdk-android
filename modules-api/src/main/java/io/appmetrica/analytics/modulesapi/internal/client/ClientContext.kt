package io.appmetrica.analytics.modulesapi.internal.client

import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext

interface ClientContext {

    val moduleAdRevenueContext: ModuleAdRevenueContext

    val clientStorageProvider: ClientStorageProvider
}
