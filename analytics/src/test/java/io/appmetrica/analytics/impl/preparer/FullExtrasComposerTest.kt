package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.protobuf.client.EventExtrasProto
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import kotlin.random.Random

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class FullExtrasComposerTest(
    val input: ByteArray?,
    val expected: List<EventProto.ReportMessage.Session.Event.ExtrasEntry>,
    val description: String
) : CommonTest() {

    companion object {
        private val emptyProtoBytes = MessageNano.toByteArray(EventExtrasProto.EventExtras())

        private val extraKey = "Some extra key"
        private val extraValue = Random(100).nextBytes(128)

        private val filledProtoBytes = MessageNano.toByteArray(
            EventExtrasProto.EventExtras().apply {
                extras = arrayOf(
                    EventExtrasProto.EventExtras.ExtrasEntry().apply {
                        key = extraKey
                        value = extraValue
                    }
                )
            }
        )

        private val emptyResultProto = emptyList<EventProto.ReportMessage.Session.Event.ExtrasEntry>()

        private val filledResultProto = listOf(
            EventProto.ReportMessage.Session.Event.ExtrasEntry().apply {
                key = extraKey.toByteArray()
                value = extraValue
            }
        )

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "for {2}")
        fun data(): Collection<Array<Any?>> =
            listOf(
                arrayOf(null, emptyResultProto, "null byte array"),
                arrayOf(ByteArray(0), emptyResultProto, "empty byte array"),
                arrayOf(emptyProtoBytes, emptyResultProto, "empty proto"),
                arrayOf(filledProtoBytes, filledResultProto, "filled proto")
            )
    }

    private lateinit var fullExtrasComposer: FullExtrasComposer

    @Before
    fun setUp() {
        fullExtrasComposer = FullExtrasComposer()
    }

    @Test
    fun getExtras() {
        assertThat(fullExtrasComposer.getExtras(input))
            .usingFieldByFieldElementComparator()
            .containsExactlyElementsOf(expected)
    }
}
