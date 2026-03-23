package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.Session

internal class RetrievedSessionResult(
    val session: Session,
    val environmentRevision: AppEnvironment.EnvironmentRevision?,
    val nextEventWithOtherEnvironment: Boolean,
    val updatedDataSize: Int,
    val updatedEventsCount: Int,
    val updatedEnvironmentSize: Int?,
)
