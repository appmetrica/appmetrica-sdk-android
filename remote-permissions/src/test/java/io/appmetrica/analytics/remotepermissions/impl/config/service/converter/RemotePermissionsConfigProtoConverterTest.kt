package io.appmetrica.analytics.remotepermissions.impl.config.service.converter

import io.appmetrica.analytics.remotepermissions.impl.config.service.model.ServiceSideRemotePermissionsConfig
import io.appmetrica.analytics.remotepermissions.impl.protobuf.client.RemotePermissionsProtobuf
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.junit.Test

internal class RemotePermissionsConfigProtoConverterTest : CommonTest() {

    private val converter = RemotePermissionsConfigProtoConverter()

    @Test
    fun `fromModel for filled`() {
        val firstPermission = "First permission"
        val secondPermission = "Second permission"
        val input = ServiceSideRemotePermissionsConfig(setOf(firstPermission, secondPermission))

        val result = converter.fromModel(input)

        ProtoObjectPropertyAssertions(result)
            .checkField(
                "permissions",
                arrayOf(firstPermission.toByteArray(), secondPermission.toByteArray())
            )
            .checkAll()
    }

    @Test
    fun `fromModel for empty set`() {
        val result = converter.fromModel(ServiceSideRemotePermissionsConfig(emptySet()))

        ProtoObjectPropertyAssertions(result)
            .checkField("permissions", emptyArray<String>())
            .checkAll()
    }

    @Test
    fun `toModel for filled`() {
        val first = "First"
        val second = "Second"
        val input = RemotePermissionsProtobuf.RemotePermissions().apply {
            permissions = arrayOf(first.toByteArray(), second.toByteArray())
        }

        val result = converter.toModel(input)

        ObjectPropertyAssertions(result)
            .checkField("permittedPermissions", setOf(first, second))
            .checkAll()
    }

    @Test
    fun `toModel for empty`() {
        val result = converter.toModel(RemotePermissionsProtobuf.RemotePermissions())

        ObjectPropertyAssertions(result)
            .checkField("permittedPermissions", emptySet<String>())
            .checkAll()
    }
}
