package io.appmetrica.analytics.impl.db.protobuf

import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto

internal class ClidsInfoStateSerializer : BaseProtobufStateSerializer<ClidsInfoProto.ClidsInfo>() {

    override fun toState(data: ByteArray): ClidsInfoProto.ClidsInfo = ClidsInfoProto.ClidsInfo.parseFrom(data)

    override fun defaultValue(): ClidsInfoProto.ClidsInfo = ClidsInfoProto.ClidsInfo()
}
