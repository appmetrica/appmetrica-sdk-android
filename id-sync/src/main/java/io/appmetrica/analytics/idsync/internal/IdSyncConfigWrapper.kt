package io.appmetrica.analytics.idsync.internal

import io.appmetrica.analytics.idsync.impl.model.IdSyncConfig

class IdSyncConfigWrapper internal constructor(
    internal val config: IdSyncConfig
) {
    companion object {
        internal fun IdSyncConfig.toWrapper() = IdSyncConfigWrapper(this)
    }
}
