package io.appmetrica.analytics.remotepermissions.impl

internal object Constants {

    const val MODULE_NAME = "rp"

    internal object Defaults {
        private val defaultProto = RemotePermissionsConfigProto()
        val DEFAULT_PERMISSIONS: Set<String> = defaultProto.permissions?.map { String(it) }?.toSet() ?: emptySet()
    }

    internal object RemoteConfig {
        const val BLOCK_NAME = "permissions"
        const val PERMISSION_BLOCK_NAME = "permissions"
        const val PERMISSION_FIELD_NAME = "name"
        const val PERMISSION_LIST_FIELD = "list"
        const val PERMISSION_ENABLED_FIELD = "enabled"
    }
}
