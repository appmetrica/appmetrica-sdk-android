package io.appmetrica.analytics.idsync.impl

internal interface IdSyncRequestCallback {

    fun onResult(result: RequestResult)
}
