package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent
import java.nio.charset.StandardCharsets

internal class BillingServiceEvent(
    override var valueBytes: ByteArray?,
) : ServiceEvent {

    override var type: Int = Constants.Events.TYPE

    override var customType: Int = 0

    override var name: String? = null

    override var value: String?
        get() = valueBytes?.let { String(it, StandardCharsets.UTF_8) }
        set(value) {
            valueBytes = value?.toByteArray(StandardCharsets.UTF_8)
        }

    override var valueProtocolVersion: Int? = null

    override var bytesTruncated: Int = 0

    override var extras: MutableMap<String, ByteArray> = mutableMapOf()
}
