package io.appmetrica.analytics.impl

import android.content.ContentValues
import io.appmetrica.analytics.impl.preparer.EventFromDbModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.EnvironmentVariable
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session.SessionDesc
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano
import org.json.JSONObject

internal class SessionResultBuilder(
    private val trimmer: Trimmer<ByteArray>,
    private val selfReporter: IReporterExtended,
) {

    private val tag = "[SessionResultBuilder]"
    private val maxEventCountPerRequest = 100
    private val environmentVariableFieldNumber = 7
    private val eventFieldNumber = 3
    private val protobufErrorEventName = "protobuf_serialization_error"

    fun build(
        sessionId: Long,
        sessionDesc: SessionDesc,
        events: List<ContentValues>,
        config: ReportRequestConfig,
        sessionNumber: Int,
        state: ReportDataSizeState,
    ): RetrievedSessionResult? {
        return try {
            buildInternal(sessionId, sessionDesc, events, config, sessionNumber, state)
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "Some problems while getting session with id = %d.", sessionId)
            selfReporter.reportError(protobufErrorEventName, ex)
            null
        }
    }

    private fun buildInternal(
        sessionId: Long,
        sessionDesc: SessionDesc,
        events: List<ContentValues>,
        config: ReportRequestConfig,
        sessionNumber: Int,
        state: ReportDataSizeState,
    ): RetrievedSessionResult? {
        val eventsOfSession = mutableListOf<Session.Event>()
        var reportDataSize = state.reportDataSize
        var eventsCount = state.eventsCount
        var environmentSize = state.environmentSize
        var latestRevision: AppEnvironment.EnvironmentRevision? = null
        var nextEventHasDifferentRevision = false

        for (contentValues in events) {
            if (eventsCount >= maxEventCountPerRequest) break

            val eventModel = EventFromDbModel(contentValues)
            val sessionEvent = getEvent(eventModel, config)
            if (sessionEvent == null) {
                DebugLogger.warning(tag, "Event #%d in session %d is null", eventsOfSession.size, sessionId)
                eventsCount++
                continue
            }

            val revision = AppEnvironment.EnvironmentRevision(
                eventModel.appEnvironment, eventModel.appEnvironmentRevision
            )
            if (latestRevision == null) {
                latestRevision = revision
                if (environmentSize == null) {
                    environmentSize = computeEnvironmentSize(latestRevision)
                    reportDataSize += environmentSize
                }
            } else if (latestRevision != revision) {
                nextEventHasDifferentRevision = true
                break
            }

            cutValueSize(sessionEvent)
            reportDataSize += CodedOutputByteBufferNano.computeMessageSize(eventFieldNumber, sessionEvent)
            if (isEventsLimitExceeded(reportDataSize, eventsOfSession.isEmpty() && sessionNumber == 0)) break

            eventsOfSession.add(sessionEvent)
            eventsCount++
        }

        if (eventsOfSession.isEmpty()) return null

        val session = Session().apply {
            id = sessionId
            this.sessionDesc = sessionDesc
            this.events = eventsOfSession.toTypedArray()
        }
        DebugLogger.info(
            tag, "Session %d, Send %d events with env %d %s",
            sessionId, eventsOfSession.size, latestRevision?.revisionNumber, latestRevision?.value
        )
        return RetrievedSessionResult(
            session, latestRevision, nextEventHasDifferentRevision,
            reportDataSize, eventsCount, environmentSize
        )
    }

    private fun getEvent(
        eventModel: EventFromDbModel,
        config: ReportRequestConfig,
    ): Session.Event? {
        return try {
            ProtobufUtils.getEventPreparer(eventModel.eventType).toSessionEvent(eventModel, config)
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "Something went wrong while getting event")
            selfReporter.reportError(protobufErrorEventName, ex)
            null
        }
    }

    private fun computeEnvironmentSize(revision: AppEnvironment.EnvironmentRevision): Int {
        return try {
            val obj = JSONObject(revision.value)
            val keys = obj.keys()
            var size = 0
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val variable = EnvironmentVariable()
                    variable.name = key
                    variable.value = obj.getString(key)
                    size += CodedOutputByteBufferNano.computeMessageSize(environmentVariableFieldNumber, variable)
                } catch (e: Throwable) {
                    DebugLogger.error(tag, e, "Something went wrong while computing environment size")
                }
            }
            size
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Something went wrong while computing environment size")
            0
        }
    }

    private fun isEventsLimitExceeded(reportDataSize: Int, isEventExtended: Boolean): Boolean {
        return if (isEventExtended) {
            reportDataSize >= EventLimitationProcessor.EXTENDED_SINGLE_EVENT_SESSION_DATA_MAX_SIZE
        } else {
            reportDataSize >= EventLimitationProcessor.SESSIONS_DATA_MAX_SIZE
        }
    }

    private fun cutValueSize(sessionEvent: Session.Event) {
        val cut = trimmer.trim(sessionEvent.value)
        if (sessionEvent.value !== cut) {
            sessionEvent.bytesTruncated += (sessionEvent.value?.size ?: 0) - (cut?.size ?: 0)
            sessionEvent.value = cut
            DebugLogger.info(tag, "truncated %d bytes", sessionEvent.bytesTruncated)
        }
    }
}
