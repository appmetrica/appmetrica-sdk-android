package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto

internal class ServiceSideContentObserverCaptorConfig(
    val enabled: Boolean,
    val mediaStoreColumnNames: List<String>,
    val detectWindowSeconds: Long,
) {

    constructor() : this(
        ContentObserverCaptorConfigProto().enabled,
        Constants.Defaults.defaultMediaStoreColumnNames,
        ContentObserverCaptorConfigProto().detectWindowSeconds,
    )

    override fun toString(): String {
        return "ServiceSideContentObserverCaptorConfig(" +
            "enabled=$enabled" +
            ", mediaStoreColumnNames=$mediaStoreColumnNames" +
            ", detectWindowSeconds=$detectWindowSeconds" +
            ")"
    }
}
