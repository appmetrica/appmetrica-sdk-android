package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class NativeCrashReportCreatorTest : CommonTest() {

    private val errorEnvironment = "Error environment"
    private val apiKey = "Api key"
    private val uuid = "uuid"
    private val creationTime = 1700000000000L

    private val metadata: AppMetricaNativeCrashMetadata = mock {
        on { errorEnvironment } doReturn errorEnvironment
        on { apiKey } doReturn apiKey
    }

    private val crash: AppMetricaNativeCrash = mock {
        on { metadata } doReturn metadata
        on { uuid } doReturn uuid
        on { creationTime } doReturn creationTime
    }

    private val eventType = InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF

    private val dump = "Some crash dump"

    private val logger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getOrCreatePublicLogger(apiKey) } doReturn logger
    }

    private val report: CounterReport = mock()

    @get:Rule
    val eventsManagerMockedStaticRule = staticRule<EventsManager> {
        on { EventsManager.nativeCrashEntry(eventType, dump, uuid, logger, creationTime) } doReturn report
    }

    private val timestampProvider: NativeCrashTimestampProvider = mock {
        on { getTimestamp(crash) } doReturn creationTime
    }

    private val reportCreator: NativeCrashReportCreator by setUp {
        NativeCrashReportCreator(crash, eventType, timestampProvider)
    }

    @Test
    fun create() {
        assertThat(reportCreator.create(dump)).isEqualTo(report)
        verify(report).eventEnvironment = errorEnvironment
    }
}
