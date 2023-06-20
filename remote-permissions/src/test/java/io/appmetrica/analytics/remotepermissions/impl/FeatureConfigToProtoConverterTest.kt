package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.remotepermissions.impl.protobuf.client.RemotePermissionsProtobuf
import org.junit.Before
import org.junit.Test

class FeatureConfigToProtoConverterTest {

    private lateinit var converter: FeatureConfigToProtoConverter

    @Before
    fun setUp() {
        converter = FeatureConfigToProtoConverter()
    }

    @Test
    fun `fromModel for filled`() {
        val firstPermission = "First permission"
        val secondPermission = "Second permission"
        val input = FeatureConfig(setOf(firstPermission, secondPermission))

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
        val result = converter.fromModel(FeatureConfig(emptySet()))

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
