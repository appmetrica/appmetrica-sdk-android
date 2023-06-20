package io.appmetrica.analytics.remotepermissions.impl

class FeatureConfig(val permittedPermissions: Set<String>) {

    override fun toString(): String {
        return "FeatureConfig(permittedPermissions=$permittedPermissions)"
    }
}
