package io.appmetrica.analytics.impl

import android.content.ContentValues
import android.database.Cursor
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReportTaskDbInteractor(component: ComponentUnit) {

    private val tag = "[ReportTaskDbInteractor]"

    private val dbHelper = component.dbHelper
    private val sessionManager = component.sessionManager
    private val vitalComponentDataProvider = component.vitalComponentDataProvider

    fun collectAllQueryParameters(): ContentValues? =
        dbHelper.collectAllQueryParameters().firstOrNull()

    fun querySessions(queryValues: Map<String, String>): Cursor? =
        dbHelper.querySessions(queryValues)

    fun queryReports(sessionId: Long, sessionType: SessionType): Cursor? =
        dbHelper.queryReports(sessionId, sessionType)

    fun getNextRequestId(): Int =
        vitalComponentDataProvider.reportRequestId + 1

    fun cleanPostedData(
        sessions: Array<Session>,
        internalSessionIds: List<Long>,
        requestId: Int,
        isBadRequest: Boolean,
    ) {
        DebugLogger.info(tag, "save request id: $requestId")
        vitalComponentDataProvider.reportRequestId = requestId
        sessions.forEachIndexed { index, session ->
            try {
                val internalSessionId = internalSessionIds[index]
                val sessionType = ProtobufUtils.sessionTypeToInternal(session.sessionDesc.sessionType)
                val maxNumberInSession = session.events.maxOfOrNull { it.numberInSession } ?: 0L
                dbHelper.removeSessionEventsUpTo(internalSessionId, sessionType.code, maxNumberInSession, isBadRequest)
                ProtobufUtils.logSessionEvents(session)
            } catch (ex: Throwable) {
                DebugLogger.error(tag, ex, "Something went wrong while removing session from db")
            }
        }
        val count = dbHelper.removeEmptySessions(sessionManager.thresholdSessionIdForActualSessions)
        DebugLogger.info(tag, "Remove $count sessions")
    }
}
