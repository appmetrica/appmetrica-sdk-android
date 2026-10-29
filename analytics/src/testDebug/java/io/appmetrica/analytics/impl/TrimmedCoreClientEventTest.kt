package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock

internal class TrimmedCoreClientEventTest : CommonTest() {

    private val logger: PublicLogger = mock()

    @Test
    fun standardTrimsFieldsAndDoesNotMutateSource() {
        val longName = "n".repeat(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH + 40)
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 12) { 7 }
        val longProfile = "p".repeat(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH + 8)
        val source = CoreClientEvent(
            name = longName,
            valueBytes = longValue,
            profileID = longProfile,
            eventEnvironment = "env",
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId,
            customType = 3,
            valueProtocolVersion = 2,
            bytesTruncated = 0,
            trimPolicy = EventTrimPolicy.STANDARD,
            extras = mapOf("k" to byteArrayOf(1)),
        )

        val trimmed = TrimmedCoreClientEvent(logger, source)

        assertThat(trimmed.name!!.length).isEqualTo(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH)
        assertThat(trimmed.valueBytes!!.size).isEqualTo(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE)
        assertThat(trimmed.profileID!!.length).isEqualTo(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH)
        val expectedNameDelta = longName.toByteArray().size - trimmed.name!!.toByteArray().size
        val expectedValueDelta = longValue.size - trimmed.valueBytes!!.size
        assertThat(trimmed.bytesTruncated).isEqualTo(expectedNameDelta + expectedValueDelta)
        assertThat(trimmed.eventEnvironment).isEqualTo("env")
        assertThat(trimmed.type).isEqualTo(InternalEvents.EVENT_TYPE_REGULAR.typeId)
        assertThat(trimmed.customType).isEqualTo(3)
        assertThat(trimmed.valueProtocolVersion).isEqualTo(2)
        assertThat(trimmed.extras).containsEntry("k", byteArrayOf(1))

        assertThat(source.name).isEqualTo(longName)
        assertThat(source.valueBytes).isEqualTo(longValue)
        assertThat(source.profileID).isEqualTo(longProfile)
        assertThat(source.bytesTruncated).isZero()
    }

    @Test
    fun nonePassthroughKeepsValuesAndExistingBytesTruncated() {
        val longName = "n".repeat(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH + 40)
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 12) { 7 }
        val source = CoreClientEvent(
            name = longName,
            valueBytes = longValue,
            bytesTruncated = 42,
            trimPolicy = EventTrimPolicy.NONE,
        )

        val trimmed = TrimmedCoreClientEvent(logger, source)

        assertThat(trimmed.name).isEqualTo(longName)
        assertThat(trimmed.valueBytes).isEqualTo(longValue)
        assertThat(trimmed.bytesTruncated).isEqualTo(42)
    }

    @Test
    fun accumulatesExistingBytesTruncatedWithFieldDeltas() {
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 9) { 4 }
        val source = CoreClientEvent(
            valueBytes = longValue,
            bytesTruncated = 55,
            trimPolicy = EventTrimPolicy.STANDARD,
        )

        val trimmed = TrimmedCoreClientEvent(logger, source)

        assertThat(trimmed.valueBytes!!.size).isEqualTo(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE)
        val expectedValueDelta = longValue.size - trimmed.valueBytes!!.size
        assertThat(trimmed.bytesTruncated).isEqualTo(55 + expectedValueDelta)
    }

    @Test
    fun shortFieldsUnchangedKeepZeroBytesTruncated() {
        val source = CoreClientEvent(
            name = "name",
            valueBytes = "value".toByteArray(),
            profileID = "pid",
            bytesTruncated = 0,
            trimPolicy = EventTrimPolicy.STANDARD,
        )

        val trimmed = TrimmedCoreClientEvent(logger, source)

        assertThat(trimmed.name).isEqualTo("name")
        assertThat(trimmed.valueBytes).isEqualTo("value".toByteArray())
        assertThat(trimmed.profileID).isEqualTo("pid")
        assertThat(trimmed.bytesTruncated).isZero()
    }

    @Test
    fun toStringVariants() {
        SoftAssertions().apply {
            assertThat(TrimmedCoreClientEvent(logger, CoreClientEvent()).toString())
                .contains("event: ")
                .contains("type: " + InternalEvents.EVENT_TYPE_INIT.info)
                .contains("value: 0 bytes")
        }.assertAll()

        val eventName = "test name"
        val eventValue = "test value"
        val filled = TrimmedCoreClientEvent(
            logger,
            CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId,
                name = eventName,
                valueBytes = eventValue.toByteArray(),
            ),
        )
        SoftAssertions().apply {
            assertThat(filled.toString())
                .contains("event: $eventName")
                .contains("type: " + InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.info)
                .contains("value: ${eventValue.toByteArray().size} bytes")
                .doesNotContain(eventValue)
        }.assertAll()

        SoftAssertions().apply {
            assertThat(
                TrimmedCoreClientEvent(
                    logger,
                    CoreClientEvent(valueBytes = null, trimPolicy = EventTrimPolicy.NONE),
                ).toString(),
            )
                .contains("value: null")
                .doesNotContain("bytes")
        }.assertAll()

        SoftAssertions().apply {
            assertThat(
                TrimmedCoreClientEvent(
                    logger,
                    CoreClientEvent(valueBytes = ByteArray(0), trimPolicy = EventTrimPolicy.NONE),
                ).toString(),
            ).contains("value: 0 bytes")
        }.assertAll()
    }
}
