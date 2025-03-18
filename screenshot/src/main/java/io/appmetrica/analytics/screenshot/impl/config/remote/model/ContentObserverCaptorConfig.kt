package io.appmetrica.analytics.screenshot.impl.config.remote.model

import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto

class ContentObserverCaptorConfig(
    val enabled: Boolean,
    val mediaStoreColumnNames: List<String>,
    val detectWindowSeconds: Long,
) {

    constructor() : this(
        ContentObserverCaptorConfigProto().enabled,
        ContentObserverCaptorConfigProto().mediaStoreColumnNames.toList(),
        ContentObserverCaptorConfigProto().detectWindowSeconds,
    )

    override fun toString(): String {
        return "ContentObserverCaptorConfig(" +
            "enabled=$enabled" +
            ", mediaStoreColumnNames='$mediaStoreColumnNames'" +
            ", detectWindowSeconds=$detectWindowSeconds" +
            ")"
    }
}
