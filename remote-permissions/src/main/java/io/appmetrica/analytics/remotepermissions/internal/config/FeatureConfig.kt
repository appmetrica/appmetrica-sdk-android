package io.appmetrica.analytics.remotepermissions.internal.config

class FeatureConfig internal constructor(val permittedPermissions: Set<String>) {

    override fun toString(): String {
        return "FeatureConfig(permittedPermissions=$permittedPermissions)"
    }
}
