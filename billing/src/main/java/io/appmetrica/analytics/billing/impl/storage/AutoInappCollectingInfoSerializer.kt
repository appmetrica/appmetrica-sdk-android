package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billing.impl.protobuf.client.AutoInappCollectingInfoProto
import io.appmetrica.analytics.coreutils.internal.data.BaseProtobufStateSerializer

class AutoInappCollectingInfoSerializer :
    BaseProtobufStateSerializer<AutoInappCollectingInfoProto.AutoInappCollectingInfo>() {

    override fun toState(data: ByteArray): AutoInappCollectingInfoProto.AutoInappCollectingInfo {
        return AutoInappCollectingInfoProto.AutoInappCollectingInfo.parseFrom(data)
    }

    override fun defaultValue(): AutoInappCollectingInfoProto.AutoInappCollectingInfo {
        return AutoInappCollectingInfoProto.AutoInappCollectingInfo()
    }
}
