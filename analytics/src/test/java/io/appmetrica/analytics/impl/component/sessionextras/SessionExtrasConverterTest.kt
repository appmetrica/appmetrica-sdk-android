package io.appmetrica.analytics.impl.component.sessionextras

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.client.SessionExtrasProtobuf
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class SessionExtrasConverterTest : CommonTest() {

    private lateinit var sessionExtrasConverter: SessionExtrasConverter

    @Before
    fun setUp() {
        sessionExtrasConverter = SessionExtrasConverter()
    }

    @Test
    fun `fromModel for empty map`() {
        val result = sessionExtrasConverter.fromModel(emptyMap())

        ProtoObjectPropertyAssertions(result)
            .checkField("extras", emptyArray<SessionExtrasProtobuf.SessionStateExtrasEntry>())
            .checkAll()
    }

    @Test
    fun `fromModel for filled`() {
        val firstExtraKey = "First extra key"
        val firstExtraValue = "First extra value".toByteArray()
        val secondExtraKey = "Second extra key"
        val secondExtraValue = "Second extra value".toByteArray()

        val input = mapOf(firstExtraKey to firstExtraValue, secondExtraKey to secondExtraValue)
        val result = sessionExtrasConverter.fromModel(input)

        ProtoObjectPropertyAssertions(result)
            .checkField(
                "extras",
                arrayOf(
                    SessionExtrasProtobuf.SessionStateExtrasEntry().apply {
                        key = firstExtraKey.toByteArray()
                        value = firstExtraValue
                    },
                    SessionExtrasProtobuf.SessionStateExtrasEntry().apply {
                        key = secondExtraKey.toByteArray()
                        value = secondExtraValue
                    }
                )
            )
            .checkAll()
    }

    @Test
    fun `toModel for empty`() {
        val result = sessionExtrasConverter.toModel(SessionExtrasProtobuf.SessionExtras())
        assertThat(result).isEmpty()
    }

    @Test
    fun `toModel for filled`() {
        val firstSessionExtraKey = "First session extra key"
        val firstSessionExtraValue = "First session extra value".toByteArray()
        val secondSessionExtraKey = "Second session extra key"
        val secondSessionExtraValue = "Second session extra value".toByteArray()

        val input = SessionExtrasProtobuf.SessionExtras().apply {
            extras = arrayOf(
                SessionExtrasProtobuf.SessionStateExtrasEntry().apply {
                    key = firstSessionExtraKey.toByteArray()
                    value = firstSessionExtraValue
                },
                SessionExtrasProtobuf.SessionStateExtrasEntry().apply {
                    key = secondSessionExtraKey.toByteArray()
                    value = secondSessionExtraValue
                }
            )
        }

        val result = sessionExtrasConverter.toModel(input)

        assertThat(result).containsExactlyEntriesOf(
            mapOf(
                firstSessionExtraKey to firstSessionExtraValue,
                secondSessionExtraKey to secondSessionExtraValue
            )
        )
    }
}
