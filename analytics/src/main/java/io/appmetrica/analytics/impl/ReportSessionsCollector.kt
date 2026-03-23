package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
import org.json.JSONObject

internal class ReportSessionsCollector(
    private val dbInteractor: ReportTaskDbInteractor,
    trimmer: Trimmer<ByteArray>,
    private val selfReporter: IReporterExtended,
) {

    private val tag = "[ReportSessionsCollector]"
    private val maxEventCountPerRequest = 100
    private val sessionIdFieldNumber = 1
    private val sessionDescFieldNumber = 2
    private val protobufErrorEventName = "protobuf_serialization_error"

    private val sessionBuilder = SessionResultBuilder(trimmer, selfReporter)

    fun collect(queryValues: Map<String, String>, config: ReportRequestConfig): ReportSessionData {
        val (candidates, initialDataSize) = collectCandidates(queryValues, config)
        val sessionIdToTypeCode = candidates.associate { it.sessionId to it.sessionTypeCode }
        val eventsBySession = dbInteractor.queryReportsForSessions(sessionIdToTypeCode, maxEventCountPerRequest)

        var reportDataSize = initialDataSize
        var eventsCount = 0
        var environmentSize: Int? = null
        var latestRevision: AppEnvironment.EnvironmentRevision? = null
        var environmentJSON = JSONObject()
        val allSessions = mutableListOf<Session>()
        val internalSessionsIds = mutableListOf<Long>()

        for (candidate in candidates) {
            if (eventsCount >= maxEventCountPerRequest) break
            val retrieved = sessionBuilder.build(
                candidate.sessionId, candidate.sessionDesc,
                eventsBySession[candidate.sessionId] ?: emptyList(),
                config, allSessions.size,
                ReportDataSizeState(reportDataSize, eventsCount, environmentSize)
            ) ?: continue

            reportDataSize = retrieved.updatedDataSize
            eventsCount = retrieved.updatedEventsCount
            environmentSize = retrieved.updatedEnvironmentSize

            if (latestRevision != null && latestRevision != retrieved.environmentRevision) break
            latestRevision = retrieved.environmentRevision
            internalSessionsIds.add(candidate.sessionId)
            allSessions.add(retrieved.session)
            val envValue = retrieved.environmentRevision?.value
            if (!envValue.isNullOrEmpty()) {
                try {
                    environmentJSON = JSONObject(envValue)
                } catch (e: Throwable) {
                    DebugLogger.error(tag, e, "Some problems while parsing environment")
                }
            }
            if (retrieved.nextEventWithOtherEnvironment) break
        }

        return ReportSessionData(allSessions, internalSessionsIds, environmentJSON)
    }

    private fun collectCandidates(
        queryValues: Map<String, String>,
        config: ReportRequestConfig,
    ): Pair<List<SessionCandidate>, Int> {
        val candidates = mutableListOf<SessionCandidate>()
        var reportDataSize = 0
        try {
            for (sessionModel in dbInteractor.querySessionModels(queryValues)) {
                if (candidates.size >= maxEventCountPerRequest) break

                val sessionId = sessionModel.id
                if (sessionId == null) {
                    DebugLogger.error(tag, "no session_id in model")
                    continue
                }

                val time = ProtobufUtils.buildTime(
                    sessionModel.description.startTime,
                    sessionModel.description.serverTimeOffset,
                    sessionModel.description.obtainedBeforeFirstSynchronization
                )
                val sessionDesc = ProtobufUtils.buildSessionDesc(config.locale, sessionModel.type, time)

                reportDataSize += CodedOutputByteBufferNano.computeUInt64Size(
                    sessionIdFieldNumber, Long.MAX_VALUE
                )
                reportDataSize += CodedOutputByteBufferNano.computeMessageSize(
                    sessionDescFieldNumber, sessionDesc
                )
                if (reportDataSize >= EventLimitationProcessor.SESSIONS_DATA_MAX_SIZE) break

                val sessionType = ProtobufUtils.sessionTypeToInternal(sessionDesc.sessionType)
                candidates.add(SessionCandidate(sessionId, sessionDesc, sessionType.code))
            }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "Some problems while getting sessions")
            selfReporter.reportError(protobufErrorEventName, ex)
        }
        return Pair(candidates, reportDataSize)
    }

    private data class SessionCandidate(
        val sessionId: Long,
        val sessionDesc: SessionDesc,
        val sessionTypeCode: Int,
    )
}
