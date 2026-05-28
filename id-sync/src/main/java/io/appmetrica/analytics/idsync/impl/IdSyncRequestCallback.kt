package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.idsync.impl.model.RequestConfig

internal interface IdSyncRequestCallback {

    fun onResult(result: RequestResult, requestConfig: RequestConfig)
}
