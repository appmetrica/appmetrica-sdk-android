package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Time
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.TimeUtils
import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ReportSessionsCollectorTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val protobufUtilsRule = staticRule<ProtobufUtils> {
        on { ProtobufUtils.buildTime(anyOrNull(), anyLong(), anyBoolean()) } doReturn Time()
        on { ProtobufUtils.buildSessionDesc(any(), anyOrNull(), any()) } doReturn SessionDesc()
        on { ProtobufUtils.sessionTypeToInternal(anyInt()) } doReturn SessionType.FOREGROUND
    }

    @get:Rule
    val sessionBuilderRule = constructionRule<SessionResultBuilder> {
        on { build(any(), any(), any(), any(), any(), any()) } doReturn null
    }

    private val sessionId = 1L
    private val sessionType = SessionType.FOREGROUND

    private val requestConfig = mock<ReportRequestConfig> {
        on { locale } doReturn "en"
    }
    private val trimmer = mock<Trimmer<ByteArray>>()
    private val selfReporter = mock<IReporterExtended>()
    private val dbInteractor = mock<ReportTaskDbInteractor>()

    private val collector by setUp { ReportSessionsCollector(dbInteractor, trimmer, selfReporter) }

    private val sessionBuilder get() = sessionBuilderRule.constructionMock.constructed()[0]

    private val queryValues = emptyMap<String, String>()

    @Before
    fun setUp() {
        whenever(dbInteractor.querySessionModels(any())).thenReturn(emptyList())
        whenever(dbInteractor.queryReportsForSessions(any(), any())).thenReturn(emptyMap())
    }

    private fun buildSessionModel(
        id: Long = sessionId,
        type: SessionType = sessionType,
    ) = DbSessionModel(
        id = id,
        type = type,
        reportRequestParameters = null,
        description = DbSessionModel.Description(
            startTime = TimeUtils.currentDeviceTimeSec(),
            serverTimeOffset = 0,
            obtainedBeforeFirstSynchronization = false,
        )
    )

    private fun buildRetrievedResult(
        id: Long = sessionId,
        environmentRevision: AppEnvironment.EnvironmentRevision? = null,
        nextEventWithOtherEnvironment: Boolean = false,
        updatedEventsCount: Int = 1,
    ) = RetrievedSessionResult(
        session = Session().apply { this.id = id; events = arrayOf(Session.Event()) },
        environmentRevision = environmentRevision,
        nextEventWithOtherEnvironment = nextEventWithOtherEnvironment,
        updatedDataSize = 100,
        updatedEventsCount = updatedEventsCount,
        updatedEnvironmentSize = 0,
    )

    @Test
    fun `collect returns empty data when sessions list is empty`() {
        val result = collector.collect(queryValues, requestConfig)
        assertThat(result.sessions).isEmpty()
        assertThat(result.internalSessionsIds).isEmpty()
    }

    @Test
    fun `collect returns empty data when sessionBuilder returns null`() {
        whenever(dbInteractor.querySessionModels(any())).thenReturn(listOf(buildSessionModel()))

        val result = collector.collect(queryValues, requestConfig)
        assertThat(result.sessions).isEmpty()
        assertThat(result.internalSessionsIds).isEmpty()
    }

    @Test
    fun `collect returns session data`() {
        whenever(dbInteractor.querySessionModels(any())).thenReturn(listOf(buildSessionModel()))
        whenever(sessionBuilder.build(any(), any(), any(), any(), any(), any()))
            .thenReturn(buildRetrievedResult())

        val result = collector.collect(queryValues, requestConfig)

        assertThat(result.sessions).hasSize(1)
        assertThat(result.sessions[0].id).isEqualTo(sessionId)
        assertThat(result.sessions[0].events?.size).isEqualTo(1)
        assertThat(result.internalSessionsIds).containsExactly(sessionId)
    }

    @Test
    fun `collect stops at MAX_EVENT_COUNT_PER_REQUEST across sessions`() {
        val id2 = sessionId + 1
        whenever(dbInteractor.querySessionModels(any()))
            .thenReturn(listOf(buildSessionModel(id = sessionId), buildSessionModel(id = id2)))
        whenever(sessionBuilder.build(any(), any(), any(), any(), any(), any()))
            .thenReturn(buildRetrievedResult(updatedEventsCount = 100))

        val result = collector.collect(queryValues, requestConfig)

        assertThat(result.sessions).hasSize(1)
        assertThat(result.internalSessionsIds).containsExactly(sessionId)
    }

    @Test
    fun `collect stops sessions when environment revision changes between sessions`() {
        val id2 = sessionId + 1
        whenever(dbInteractor.querySessionModels(any()))
            .thenReturn(listOf(buildSessionModel(id = sessionId), buildSessionModel(id = id2)))

        val revision1 = AppEnvironment.EnvironmentRevision("{}", 0)
        val revision2 = AppEnvironment.EnvironmentRevision("{\"k\":\"v\"}", 1)

        whenever(sessionBuilder.build(any(), any(), any(), any(), any(), any()))
            .thenReturn(buildRetrievedResult(id = sessionId, environmentRevision = revision1))
            .thenReturn(buildRetrievedResult(id = id2, environmentRevision = revision2))

        val result = collector.collect(queryValues, requestConfig)

        assertThat(result.sessions).hasSize(1)
        assertThat(result.internalSessionsIds).containsExactly(sessionId)
    }
}
