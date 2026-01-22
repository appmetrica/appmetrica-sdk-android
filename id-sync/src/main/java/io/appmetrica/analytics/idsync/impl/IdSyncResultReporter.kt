package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers

internal interface IdSyncResultReporter {
    fun reportResult(value: String, sdkIdentifiers: SdkIdentifiers)
}
