package io.appmetrica.analytics.remotepermissions.impl.config.service.parser

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

internal class RemotePermissionsConfigJsonParserTest : CommonTest() {

    private val block = "permissions"
    private val permissionName = "name"
    private val list = "list"
    private val enabled = "enabled"

    private val firstPermittedPermission = "PermittedPermission"
    private val secondPermittedPermission = "SecondPermittedPermission"
    private val deniedPermission = "DeniedPermission"
    private val permissionWithMissingValue = "PermissionWithMissingValue"

    private val parser = RemotePermissionsConfigJsonParser()

    @Test
    fun `parse for empty json`() {
        ProtoObjectPropertyAssertions(parser.parse(JSONObject()))
            .checkField("permissions", emptyArray<String>())
            .checkAll()
    }

    @Test
    fun `parse for empty block`() {
        val input = JSONObject().put(block, JSONObject())

        ProtoObjectPropertyAssertions(parser.parse(input))
            .checkField("permissions", emptyArray<String>())
            .checkAll()
    }

    @Test
    fun `parse for empty list`() {
        val input = JSONObject().put(
            block,
            JSONObject()
                .put(list, JSONArray())
        )

        ProtoObjectPropertyAssertions(parser.parse(input))
            .checkField("permissions", emptyArray<String>())
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

        ProtoObjectPropertyAssertions(parser.parse(input))
            .checkField(
                "permissions",
                arrayOf(firstPermittedPermission.toByteArray(), secondPermittedPermission.toByteArray())
            )
            .checkAll()
    }
}
