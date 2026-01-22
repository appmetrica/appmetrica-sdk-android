package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class FeatureParserTest : CommonTest() {

    private val block = "permissions"
    private val permissionName = "name"
    private val list = "list"
    private val enabled = "enabled"

    private val firstPermittedPermission = "PermittedPermission"
    private val secondPermittedPermission = "SecondPermittedPermission"
    private val deniedPermission = "DeniedPermission"
    private val permissionWithMissingValue = "PermissionWithMissingValue"

    private lateinit var featureParser: FeatureParser

    @Before
    fun setUp() {
        featureParser = FeatureParser()
    }

    @Test
    fun `parse for empty json`() {
        val result = featureParser.parse(JSONObject())

        ObjectPropertyAssertions(result)
            .checkField("permittedPermissions", emptySet<String>())
            .checkAll()
    }

    @Test
    fun `parse for empty block`() {
        val input = JSONObject().put(block, JSONObject())
        val result = featureParser.parse(input)

        ObjectPropertyAssertions(result)
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
        val result = featureParser.parse(input)

        ObjectPropertyAssertions(result)
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
        val result = featureParser.parse(input)

        ObjectPropertyAssertions(result)
            .checkField("permittedPermissions", setOf(firstPermittedPermission, secondPermittedPermission))
            .checkAll()
    }
}
