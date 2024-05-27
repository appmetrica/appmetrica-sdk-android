package io.appmetrica.analytics.impl.utils

import android.text.TextUtils
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.protobuf.backend.EventProto

object PublicLogConstructor {

    @JvmStatic
    fun constructCounterReportLog(reportData: CounterReport, message: String): String? {
        return if (EventsManager.isPublicForLogs(reportData.type)) {
            buildString {
                append(message)
                append(": ")
                append(reportData.name)
                if (EventsManager.shouldLogValue(reportData.type) && !TextUtils.isEmpty(reportData.value)) {
                    append(" with value ")
                    append(reportData.value)
                }
            }
        } else {
            null
        }
    }

    @JvmStatic
    fun constructEventLog(event: EventProto.ReportMessage.Session.Event, message: String): String? {
        return if (EventsManager.isSuitableForLogs(event)) {
            "$message: ${getLogValue(event)}"
        } else {
            null
        }
    }

    private fun getLogValue(event: EventProto.ReportMessage.Session.Event): String {
        return if (event.type == EventProto.ReportMessage.Session.Event.EVENT_CRASH && TextUtils.isEmpty(event.name)) {
            "Native crash of app"
        } else if (event.type == EventProto.ReportMessage.Session.Event.EVENT_CLIENT) {
            val logValue = java.lang.StringBuilder(event.name)
            if (event.value != null) {
                val value = String(event.value)
                if (!TextUtils.isEmpty(value)) {
                    logValue.append(" with value ")
                    logValue.append(value)
                }
            }
            logValue.toString()
        } else {
            event.name
        }
    }
}
