package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.client.EventExtrasProto
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class EventExtrasConverterTest : CommonTest() {

    private val random = Random(100)
    private lateinit var eventExtrasConverter: EventExtrasConverter

    @Before
    fun setUp() {
        eventExtrasConverter = EventExtrasConverter()
    }

    @Test
    fun `fromModel for filled`() {
        val firstKey = "first test key"
        val firstValue = random.nextBytes(10)
        val secondKey = "second test key"
        val secondValue = ByteArray(0)

        val input = mapOf(firstKey to firstValue, secondKey to secondValue)
        val result = eventExtrasConverter.fromModel(input)

        val expectedEntries = arrayOf(
            EventExtrasProto.EventExtras.ExtrasEntry().apply {
                key = firstKey
                value = firstValue
            },
            EventExtrasProto.EventExtras.ExtrasEntry().apply {
                key = secondKey
                value = secondValue
            }
        )

        ProtoObjectPropertyAssertions(EventExtrasProto.EventExtras.parseFrom(result))
            .checkField("extras", expectedEntries)
            .checkAll()
    }

    @Test
    fun `fromModel for empty`() {
        val result = eventExtrasConverter.fromModel(emptyMap())

        ProtoObjectPropertyAssertions(EventExtrasProto.EventExtras.parseFrom(result))
            .checkField("extras", emptyArray<EventExtrasProto.EventExtras.ExtrasEntry>())
            .checkAll()
    }

    @Test
    fun `toModel for filled`() {
        val firstKey = "first key"
        val firstValue = random.nextBytes(10)
        val secondKey = "second key"
        val secondValue = ByteArray(0)

        val input = EventExtrasProto.EventExtras().apply {
            extras = arrayOf(
                EventExtrasProto.EventExtras.ExtrasEntry().apply {
                    key = firstKey
                    value = firstValue
                },
                EventExtrasProto.EventExtras.ExtrasEntry().apply {
                    key = secondKey
                    value = secondValue
                }
            )
        }

        assertThat(eventExtrasConverter.toModel(MessageNano.toByteArray(input)))
            .containsExactlyInAnyOrderEntriesOf(mapOf(firstKey to firstValue, secondKey to secondValue))
    }

    @Test
    fun `toModel for empty`() {
        assertThat(eventExtrasConverter.toModel(MessageNano.toByteArray(EventExtrasProto.EventExtras()))).isEmpty()
    }
}
