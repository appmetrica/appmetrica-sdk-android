package io.appmetrica.analytics.impl

import android.content.ContentValues
import io.appmetrica.analytics.coreutils.internal.db.DBUtils
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.SessionEventsDeleteParams
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.LinkedHashMap

internal class ReportTaskDbInteractor(component: ComponentUnit) {

    private val tag = "[ReportTaskDbInteractor]"

    private val dbHelper = component.dbHelper
    private val sessionManager = component.sessionManager
    private val vitalComponentDataProvider = component.vitalComponentDataProvider

    fun collectAllQueryParameters(): ContentValues? =
        dbHelper.collectAllQueryParameters().firstOrNull()

    fun querySessionModels(queryValues: Map<String, String>): List<DbSessionModel> {
        val cursor = dbHelper.querySessions(queryValues) ?: return emptyList()
        val result = mutableListOf<DbSessionModel>()
        try {
            cursor.use {
                while (it.moveToNext()) {
                    val cv = ContentValues()
                    DBUtils.cursorRowToContentValues(it, cv)
                    result.add(DbSessionModelConverter().toModel(cv))
                }
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Something went wrong while loading sessions")
        }
        return result
    }

    fun queryReportsForSessions(sessionIdToTypeCode: Map<Long, Int>, limit: Int): Map<Long, List<ContentValues>> {
        val cursor = dbHelper.queryReportsForSessions(sessionIdToTypeCode, limit) ?: return emptyMap()
        val result = LinkedHashMap<Long, MutableList<ContentValues>>()
        try {
            cursor.use {
                while (it.moveToNext()) {
                    val cv = ContentValues()
                    DBUtils.cursorRowToContentValues(it, cv)
                    val sessionId = cv.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION)
                    result.getOrPut(sessionId) { mutableListOf() }.add(cv)
                }
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Something went wrong while loading events for sessions")
        }
        return result
    }

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
        val deleteParams = sessions.mapIndexed { index, session ->
            val internalSessionId = internalSessionIds[index]
            val sessionType = ProtobufUtils.sessionTypeToInternal(session.sessionDesc.sessionType)
            val maxNumberInSession = session.events.maxOfOrNull { it.numberInSession } ?: 0L
            ProtobufUtils.logSessionEvents(session)
            SessionEventsDeleteParams(internalSessionId, sessionType.code, maxNumberInSession, isBadRequest)
        }
        dbHelper.removeSessionsEventsUpTo(deleteParams, sessionManager.thresholdSessionIdForActualSessions)
    }
}
