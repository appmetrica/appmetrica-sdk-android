package io.appmetrica.analytics.remotepermissions.impl.config.service.parser

import android.text.TextUtils
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.remotepermissions.impl.Constants
import io.appmetrica.analytics.remotepermissions.impl.RemotePermissionsConfigProto
import org.json.JSONObject

internal class RemotePermissionsConfigJsonParser : JsonParser<RemotePermissionsConfigProto> {

    override fun parse(rawData: JSONObject): RemotePermissionsConfigProto {
        val permittedPermissions = mutableSetOf<ByteArray>()
        rawData.optJSONObject(Constants.RemoteConfig.PERMISSION_BLOCK_NAME)
            ?.optJSONArray(Constants.RemoteConfig.PERMISSION_LIST_FIELD)
            ?.let { permissionsJsonArray ->
                repeat(permissionsJsonArray.length()) { index ->
                    permissionsJsonArray.optJSONObject(index)?.let { permissionJson ->
                        if (permissionJson.optBoolean(Constants.RemoteConfig.PERMISSION_ENABLED_FIELD)) {
                            val permissionName = permissionJson.optString(Constants.RemoteConfig.PERMISSION_FIELD_NAME)
                            if (!TextUtils.isEmpty(permissionName)) {
                                permittedPermissions.add(permissionName.toByteArray())
                            }
                        }
                    }
                }
            }
        return RemotePermissionsConfigProto().apply {
            permissions = permittedPermissions.toTypedArray()
        }
    }
}
