package io.appmetrica.analytics.remotepermissions.impl

import android.text.TextUtils
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import org.json.JSONObject

internal class FeatureParser : JsonParser<FeatureConfig> {

    private val block = "permissions"
    private val permissionName = "name"
    private val list = "list"
    private val enabled = "enabled"

    override fun parse(rawData: JSONObject): FeatureConfig {
        val permittedPermissions = HashSet<String>()
        rawData.optJSONObject(block)?.optJSONArray(list)?.let { permissionsJsonArray ->
            repeat(permissionsJsonArray.length()) { index ->
                permissionsJsonArray.optJSONObject(index)?.let { permissionJson ->
                    if (permissionJson.optBoolean(enabled)) {
                        val permissionName = permissionJson.optString(permissionName)
                        if (!TextUtils.isEmpty(permissionName)) {
                            permittedPermissions.add(permissionName)
                        }
                    }
                }
            }
        }
        return FeatureConfig(permittedPermissions)
    }
}
