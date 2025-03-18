package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.config.remote.model.ContentObserverCaptorConfig

class ServiceSideContentObserverCaptorConfig(
    val enabled: Boolean,
    val mediaStoreColumnNames: List<String>,
    val detectWindowSeconds: Long,
) {

    constructor(remote: ContentObserverCaptorConfig) : this(
        remote.enabled,
        remote.mediaStoreColumnNames,
        remote.detectWindowSeconds,
    )

    override fun toString(): String {
        return "ServiceSideContentObserverCaptorConfig(" +
            "enabled=$enabled" +
            ", mediaStoreColumnNames=$mediaStoreColumnNames" +
            ", detectWindowSeconds=$detectWindowSeconds" +
            ")"
    }
}
