package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.protobuf.backend.EventProto

internal interface ExtrasComposer {

    fun getExtras(input: ByteArray?): Array<EventProto.ReportMessage.Session.Event.ExtrasEntry>
}
