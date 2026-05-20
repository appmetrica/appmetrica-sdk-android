package io.appmetrica.analytics.remotepermissions.impl.config.service

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

internal class ServiceSideRemotePermissionsConfigParserTest : CommonTest() {

    private val block = "permissions"
    private val permissionName = "name"
    private val list = "list"
    private val enabled = "enabled"

    private val firstPermittedPermission = "PermittedPermission"
    private val secondPermittedPermission = "SecondPermittedPermission"
    private val deniedPermission = "DeniedPermission"
    private val permissionWithMissingValue = "PermissionWithMissingValue"

    private val parser = ServiceSideRemotePermissionsConfigParser()

    @Test
    fun `parse for empty json`() {
        val result = parser.parse(JSONObject())

        ObjectPropertyAssertions(result.config)
            .checkField("permittedPermissions", emptySet<String>())
            .checkAll()
    }

    @Test
    fun `parse for empty block`() {
        val input = JSONObject().put(block, JSONObject())
        val result = parser.parse(input)

        ObjectPropertyAssertions(result.config)
            .checkField("permittedPermissions", emptySet<String>())
            .checkAll()
    }

    @Test
    fun `parse for empty list`() {
        val input = JSONObject().put(
            block,
            JSONObject()
                .put(list, JSONArray())
        )
        val result = parser.parse(input)

        ObjectPropertyAssertions(result.config)
            .checkField("permittedPermissions", emptySet<String>())
            .checkAll()
    }

    @Test
    fun `parse for filled`() {
        val input = JSONObject().put(
            block,
            JSONObject()
                .put(
                    list,
                    JSONArray()
                        .put(
                            JSONObject()
                                .put(permissionName, firstPermittedPermission)
                                .put(enabled, true)
                        )
                        .put(
                            JSONObject()
                        )
                        .put(
                            JSONObject()
                                .put(permissionName, deniedPermission)
                                .put(enabled, false)
                        )
                        .put(
                            JSONObject()
                                .put(permissionName, permissionWithMissingValue)
                        )
                        .put(
                            JSONObject()
                                .put(permissionName, secondPermittedPermission)
                                .put(enabled, true)
                        )
                )
        )
        val result = parser.parse(input)

        ObjectPropertyAssertions(result.config)
            .checkField("permittedPermissions", setOf(firstPermittedPermission, secondPermittedPermission))
            .checkAll()
    }
}
