package io.appmetrica.analytics.impl

import android.database.MatrixCursor
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ReportTaskDbInteractorTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val protobufUtilsRule = staticRule<ProtobufUtils> {
        on { ProtobufUtils.sessionTypeToInternal(SessionDesc.SESSION_FOREGROUND) } doReturn SessionType.FOREGROUND
        on { ProtobufUtils.sessionTypeToInternal(SessionDesc.SESSION_BACKGROUND) } doReturn SessionType.BACKGROUND
    }

    private val sessionId = 100L
    private val sessionRemovingThreshold = 42L
    private val prevRequestId = 7

    private val dbHelper = mock<DatabaseHelper>()
    private val sessionManager = mock<SessionManagerStateMachine> {
        on { thresholdSessionIdForActualSessions } doReturn sessionRemovingThreshold
    }
    private val vitalComponentDataProvider = mock<VitalComponentDataProvider> {
        on { reportRequestId } doReturn prevRequestId
    }
    private val componentUnit = mock<ComponentUnit> {
        on { dbHelper } doReturn dbHelper
        on { sessionManager } doReturn sessionManager
        on { vitalComponentDataProvider } doReturn vitalComponentDataProvider
    }
    private val queryValues = mapOf("key" to "value")

    private val interactor by setUp { ReportTaskDbInteractor(componentUnit) }

    @Test
    fun collectAllQueryParameters() {
        val expected = android.content.ContentValues()
        whenever(dbHelper.collectAllQueryParameters()).thenReturn(listOf(expected))
        assertThat(interactor.collectAllQueryParameters()).isSameAs(expected)
    }

    @Test
    fun `collectAllQueryParameters returns null when db returns empty list`() {
        whenever(dbHelper.collectAllQueryParameters()).thenReturn(emptyList())
        assertThat(interactor.collectAllQueryParameters()).isNull()
    }

    @Test
    fun querySessions() {
        val cursor = MatrixCursor(emptyArray())
        whenever(dbHelper.querySessions(queryValues)).thenReturn(cursor)
        assertThat(interactor.querySessions(queryValues)).isEqualTo(cursor)
    }

    @Test
    fun queryReports() {
        val cursor = MatrixCursor(emptyArray())
        whenever(dbHelper.queryReports(sessionId, SessionType.FOREGROUND)).thenReturn(cursor)
        assertThat(interactor.queryReports(sessionId, SessionType.FOREGROUND)).isEqualTo(cursor)
    }

    @Test
    fun getNextRequestId() {
        assertThat(interactor.getNextRequestId()).isEqualTo(prevRequestId + 1)
    }

    @Test
    fun `cleanPostedData removes events up to max numberInSession per session and removes empty sessions`() {
        val requestId = 42
        val session1 = buildSession(SessionDesc.SESSION_FOREGROUND, longArrayOf(3L, 7L, 5L))
        val session2 = buildSession(SessionDesc.SESSION_BACKGROUND, longArrayOf(10L, 2L))
        val internalSessionIds = listOf(sessionId, sessionId + 1)

        interactor.cleanPostedData(arrayOf(session1, session2), internalSessionIds, requestId, isBadRequest = false)

        verify(vitalComponentDataProvider).reportRequestId = requestId
        verify(dbHelper).removeSessionEventsUpTo(sessionId, SessionType.FOREGROUND.code, 7L, false)
        verify(dbHelper).removeSessionEventsUpTo(sessionId + 1, SessionType.BACKGROUND.code, 10L, false)
        verify(dbHelper).removeEmptySessions(sessionRemovingThreshold)
    }

    @Test
    fun `cleanPostedData passes isBadRequest true`() {
        val requestId = 55
        val session = buildSession(SessionDesc.SESSION_FOREGROUND, longArrayOf(1L))
        interactor.cleanPostedData(arrayOf(session), listOf(sessionId), requestId, isBadRequest = true)
        verify(vitalComponentDataProvider).reportRequestId = requestId
        verify(dbHelper).removeSessionEventsUpTo(any(), any(), any(), eq(true))
    }

    @Test
    fun `cleanPostedData with empty sessions only removes empty sessions`() {
        val requestId = 99
        interactor.cleanPostedData(emptyArray(), emptyList(), requestId, isBadRequest = false)
        verify(vitalComponentDataProvider).reportRequestId = requestId
        verify(dbHelper, never()).removeSessionEventsUpTo(any(), any(), any(), any())
        verify(dbHelper).removeEmptySessions(sessionRemovingThreshold)
    }

    private fun buildSession(
        protoSessionType: Int,
        numberInSessionValues: LongArray,
    ): Session {
        val sessionDesc = SessionDesc().apply {
            this.sessionType = protoSessionType
        }
        return Session().apply {
            id = sessionId
            this.sessionDesc = sessionDesc
            events = Array(numberInSessionValues.size) { i ->
                Session.Event().apply { numberInSession = numberInSessionValues[i] }
            }
        }
    }
}
