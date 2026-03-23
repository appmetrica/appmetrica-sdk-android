package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session
import org.json.JSONObject

internal class ReportSessionData(
    val sessions: List<Session>,
    val internalSessionsIds: List<Long>,
    val environment: JSONObject,
)
