package io.appmetrica.analytics.impl.utils.limitation

import io.appmetrica.analytics.coreutils.internal.limitation.BytesTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.DummyTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.StringTrimmer
import io.appmetrica.analytics.impl.EventTrimPolicy
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.data.RandomStringGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class EventFieldTrimmerTest : CommonTest() {

    private val logger: PublicLogger = mock()

    @Test
    fun nameTrimmerNoneIsDummy() {
        val trimmer = EventFieldTrimmer.nameTrimmer(logger, EventTrimPolicy.NONE)
        assertThat(trimmer).isInstanceOf(DummyTrimmer::class.java)
        val longName = generateString(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH + 10)
        assertThat(trimmer.trim(longName)).isEqualTo(longName)
    }

    @Test
    fun nameTrimmerStandardIsStringTrimmer() {
        val trimmer = EventFieldTrimmer.nameTrimmer(logger, EventTrimPolicy.STANDARD)
        assertThat(trimmer).isInstanceOf(StringTrimmer::class.java)
        assertThat((trimmer as StringTrimmer).maxSize)
            .isEqualTo(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH)
    }

    @Test
    fun valueBytesTrimmerNoneIsDummy() {
        val trimmer = EventFieldTrimmer.valueBytesTrimmer(logger, EventTrimPolicy.NONE)
        assertThat(trimmer).isInstanceOf(DummyTrimmer::class.java)
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 5) { 1 }
        assertThat(trimmer.trim(longValue)).isEqualTo(longValue)
    }

    @Test
    fun valueBytesTrimmerExtendedUsesExtendedMaxSize() {
        val trimmer = EventFieldTrimmer.valueBytesTrimmer(logger, EventTrimPolicy.EXTENDED)
        assertThat(trimmer).isInstanceOf(BytesTrimmer::class.java)
        assertThat((trimmer as BytesTrimmer).maxSize)
            .isEqualTo(EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE)
    }

    @Test
    fun valueBytesTrimmerStandardUsesReportValueMaxSize() {
        val trimmer = EventFieldTrimmer.valueBytesTrimmer(logger, EventTrimPolicy.STANDARD)
        assertThat(trimmer).isInstanceOf(BytesTrimmer::class.java)
        assertThat((trimmer as BytesTrimmer).maxSize)
            .isEqualTo(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE)
    }

    @Test
    fun profileIdTrimmerNoneIsDummy() {
        val trimmer = EventFieldTrimmer.profileIdTrimmer(logger, EventTrimPolicy.NONE)
        assertThat(trimmer).isInstanceOf(DummyTrimmer::class.java)
        val longId = generateString(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH + 10)
        assertThat(trimmer.trim(longId)).isEqualTo(longId)
    }

    @Test
    fun profileIdTrimmerStandardIsStringTrimmer() {
        val trimmer = EventFieldTrimmer.profileIdTrimmer(logger, EventTrimPolicy.STANDARD)
        assertThat(trimmer).isInstanceOf(StringTrimmer::class.java)
        assertThat((trimmer as StringTrimmer).maxSize)
            .isEqualTo(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH)
    }

    private fun generateString(size: Int): String = RandomStringGenerator(size).nextString()
}
