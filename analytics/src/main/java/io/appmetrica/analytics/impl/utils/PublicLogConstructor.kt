package io.appmetrica.analytics.impl.utils

import android.text.TextUtils
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads
import io.appmetrica.analytics.impl.protobuf.backend.EventProto

object PublicLogConstructor {

    @JvmStatic
    fun constructAnrLog(message: String, allThreads: AllThreads): String {
        return message + allThreads.affectedThread?.let { affectedThread ->
            "Thread[" +
                "name=${affectedThread.name}," +
                "tid={${affectedThread.tid}, " +
                "priority=${affectedThread.priority}, " +
                "group=${affectedThread.group}}" +
                "] at " + affectedThread.stacktrace.joinToString("\n")
        }
    }

    @JvmStatic
    fun constructCounterReportLog(reportData: CounterReport, message: String): String? =
        constructLogValueForInternalEvent(
            message,
            InternalEvents.valueOf(reportData.type),
            reportData.name,
            reportData.value
        )

    @JvmStatic
    fun constructLogValueForInternalEvent(message: String, type: InternalEvents, name: String?, value: String?) =
        if (EventsManager.isPublicForLogs(type.typeId)) {
            buildString {
                append(message)
                append(": ")
                append(type.name)
                if (EventsManager.shouldLogName(type) && !TextUtils.isEmpty(name)) {
                    append(" with name ")
                    append(name)
                }
                if (EventsManager.shouldLogValue(type) && !TextUtils.isEmpty(value)) {
                    append(" with value ")
                    append(value)
                }
            }
        } else {
            null
        }

    @JvmStatic
    fun constructEventLogForProtoEvent(event: EventProto.ReportMessage.Session.Event, message: String): String =
        "$message: ${getLogValueForProtoEvent(event)}"

    private fun getLogValueForProtoEvent(event: EventProto.ReportMessage.Session.Event): String =
        getLogValueForProto(event.type, event.name, event.value)

    private fun getLogValueForProto(type: Int, name: String?, value: ByteArray?): String = when (type) {
        EventProto.ReportMessage.Session.Event.EVENT_INIT -> "Attribution"
        EventProto.ReportMessage.Session.Event.EVENT_START -> "Session start"
        EventProto.ReportMessage.Session.Event.EVENT_CLIENT -> {
            val logValue = StringBuilder(name ?: "null")
            value?.let {
                val stringValue = String(it)
                if (!TextUtils.isEmpty(stringValue)) {
                    logValue.append(" with value ")
                    logValue.append(stringValue)
                }
            }
            logValue.toString()
        }

        EventProto.ReportMessage.Session.Event.EVENT_REFERRER -> "Referrer"
        EventProto.ReportMessage.Session.Event.EVENT_ALIVE -> "Session heartbeat"
        EventProto.ReportMessage.Session.Event.EVENT_FIRST -> "The very first event"
        EventProto.ReportMessage.Session.Event.EVENT_OPEN -> "Open"
        EventProto.ReportMessage.Session.Event.EVENT_UPDATE -> "Update"
        EventProto.ReportMessage.Session.Event.EVENT_PROFILE -> "User profile update"
        EventProto.ReportMessage.Session.Event.EVENT_REVENUE -> "Revenue"
        EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ANR -> "ANR"
        EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH -> "Crash: $name"
        EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ERROR -> "Error: $name"
        EventProto.ReportMessage.Session.Event.EVENT_ECOMMERCE -> "E-Commerce"
        EventProto.ReportMessage.Session.Event.EVENT_AD_REVENUE -> "Ad revenue (ILRD)"
        EventProto.ReportMessage.Session.Event.EVENT_CLIENT_EXTERNAL_ATTRIBUTION -> "External attribution"
        else -> "type=$type"
    }
}
