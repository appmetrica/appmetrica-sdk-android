package io.appmetrica.analytics.impl.component.sessionextras

import io.appmetrica.analytics.coreutils.internal.data.BaseProtobufStateSerializer
import io.appmetrica.analytics.impl.protobuf.client.SessionExtrasProtobuf

internal class SessionExtrasSerializer : BaseProtobufStateSerializer<SessionExtrasProtobuf.SessionExtras>() {

    override fun toState(data: ByteArray): SessionExtrasProtobuf.SessionExtras =
        SessionExtrasProtobuf.SessionExtras.parseFrom(data)

    override fun defaultValue(): SessionExtrasProtobuf.SessionExtras =
        SessionExtrasProtobuf.SessionExtras()
}
