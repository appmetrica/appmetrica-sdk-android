package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.protobuf.backend.EventProto

internal class PreparedReport(
    val reportMessage: EventProto.ReportMessage,
    val internalSessionsIds: List<Long>,
    val requestId: Int,
)
