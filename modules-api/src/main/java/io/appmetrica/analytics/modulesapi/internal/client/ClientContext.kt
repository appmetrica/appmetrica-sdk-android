package io.appmetrica.analytics.modulesapi.internal.client

import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenueReporter

interface ClientContext {

    val autoAdRevenueReporter: AutoAdRevenueReporter
}
