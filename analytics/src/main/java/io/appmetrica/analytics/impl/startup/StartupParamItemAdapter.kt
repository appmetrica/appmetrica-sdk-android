package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.StartupParamsItem

class StartupParamItemAdapter {

    private val startupParamItemStatusAdapter = StartupParamItemStatusAdapter()

    fun adapt(input: IdentifiersResult): StartupParamsItem = StartupParamsItem(
        id = input.id,
        status = startupParamItemStatusAdapter.adapt(input.status),
        errorDetails = input.errorExplanation
    )
}
