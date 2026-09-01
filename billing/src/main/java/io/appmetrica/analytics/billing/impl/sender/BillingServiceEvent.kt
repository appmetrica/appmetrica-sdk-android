package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent

internal class BillingServiceEvent(
    override var valueBytes: ByteArray?,
) : ServiceEvent {

    override var type: Int = Constants.Events.TYPE

    override var customType: Int = 0

    override var name: String? = null

    override var value: String? = null

    override var valueProtocolVersion: Int? = null

    override var bytesTruncated: Int = 0

    override var extras: MutableMap<String, ByteArray> = mutableMapOf()
}
