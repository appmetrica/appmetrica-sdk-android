package io.appmetrica.analytics.impl

import android.content.ContentValues
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.event.DbEventModel.Description
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.preparer.EventPreparer
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class SessionResultBuilderTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val mockEventPreparer = mock<EventPreparer>()

    @get:Rule
    val protobufUtilsRule = staticRule<ProtobufUtils> {
        on { ProtobufUtils.buildTime(anyOrNull(), anyLong(), anyBoolean()) } doReturn
            io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Time()
        on { ProtobufUtils.buildSessionDesc(any(), anyOrNull(), any()) } doReturn SessionDesc()
        on { ProtobufUtils.sessionTypeToInternal(anyInt()) } doReturn SessionType.FOREGROUND
        on { ProtobufUtils.getEventPreparer(anyOrNull()) } doReturn mockEventPreparer
    }

    private val sessionId = 1L

    private val requestConfig = mock<ReportRequestConfig> {
        on { locale } doReturn "en"
        on { uuid } doReturn "test-uuid"
        on { deviceId } doReturn "test-device-id"
        on { autoCollectedDataSubscribers } doReturn emptySet()
    }
    private val trimmer = mock<io.appmetrica.analytics.impl.utils.limitation.Trimmer<ByteArray>> {
        on { trim(any()) } doAnswer { it.arguments[0] as ByteArray }
    }
    private val selfReporter = mock<IReporterExtended>()

    private val builder = SessionResultBuilder(trimmer, selfReporter)

    private val contentValuesToModel = mutableMapOf<ContentValues, DbEventModel>()

    @get:Rule
    val converterRule = MockedConstructionRule(DbEventModelConverter::class.java) { mock, _ ->
        whenever(mock.toModel(any())).thenAnswer { inv ->
            val cv = inv.arguments[0] as ContentValues
            contentValuesToModel[cv] ?: run {
                val defaultDescription = mock<Description>().also {
                    whenever(it.appEnvironment).thenReturn("{}")
                    whenever(it.appEnvironmentRevision).thenReturn(0L)
                }
                mock<DbEventModel>().also { whenever(it.description).thenReturn(defaultDescription) }
            }
        }
    }

    @Before
    fun setUp() {
        whenever(mockEventPreparer.toSessionEvent(any(), any())).thenReturn(
            Session.Event().apply { value = ByteArray(0) }
        )
    }

    private fun buildEventContentValues(sessionId: Long): ContentValues = ContentValues().apply {
        put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, sessionId)
        put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE, SessionType.FOREGROUND.code)
        put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, 0)
        put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, InternalEvents.EVENT_TYPE_REGULAR.typeId)
        put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER, 1)
        put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME, 0)
    }

    private fun buildDbEventModel(
        appEnvironment: String = "{}",
        appEnvironmentRevision: Long = 0L,
    ): DbEventModel {
        val description = mock<Description>()
        whenever(description.appEnvironment).thenReturn(appEnvironment)
        whenever(description.appEnvironmentRevision).thenReturn(appEnvironmentRevision)
        return mock<DbEventModel>().also { whenever(it.description).thenReturn(description) }
    }

    @Test
    fun `build returns null when events list is empty`() {
        val state = ReportDataSizeState(0, 0, null)
        val result = builder.build(sessionId, SessionDesc(), emptyList(), requestConfig, 0, state)
        assertThat(result).isNull()
    }

    @Test
    fun `build returns session with events and updated counts`() {
        val eventCv = buildEventContentValues(sessionId)
        val state = ReportDataSizeState(0, 0, null)

        val result = builder.build(sessionId, SessionDesc(), listOf(eventCv), requestConfig, 0, state)

        assertThat(result).isNotNull
        assertThat(result!!.session.id).isEqualTo(sessionId)
        assertThat(result.session.events).hasSize(1)
        assertThat(result.updatedEventsCount).isEqualTo(1)
        assertThat(result.updatedDataSize).isGreaterThan(0)
    }

    @Test
    fun `build returns null when size limit already exceeded`() {
        val state = ReportDataSizeState(EventLimitationProcessor.SESSIONS_DATA_MAX_SIZE, 0, 0)
        val eventCv = buildEventContentValues(sessionId)

        val result = builder.build(
            sessionId, SessionDesc(), listOf(eventCv), requestConfig,
            1, // sessionNumber != 0 so not extended limit
            state
        )
        assertThat(result).isNull()
    }

    @Test
    fun `build stops and sets nextEventWithOtherEnvironment when revision changes within session`() {
        val cv1 = buildEventContentValues(sessionId)
        val cv2 = buildEventContentValues(sessionId)
        contentValuesToModel[cv1] = buildDbEventModel(appEnvironment = "{}", appEnvironmentRevision = 0L)
        contentValuesToModel[cv2] = buildDbEventModel(appEnvironment = "{\"k\":\"v\"}", appEnvironmentRevision = 1L)

        val state = ReportDataSizeState(0, 0, null)
        val result = builder.build(sessionId, SessionDesc(), listOf(cv1, cv2), requestConfig, 0, state)

        assertThat(result).isNotNull
        assertThat(result!!.session.events).hasSize(1)
        assertThat(result.nextEventWithOtherEnvironment).isTrue()
    }
}
