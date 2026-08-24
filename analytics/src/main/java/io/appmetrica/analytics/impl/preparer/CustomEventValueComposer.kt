package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.request.ReportRequestConfig

internal class CustomEventValueComposer(
    val valueComposer: ValueComposer,
    val legacyValueComposer: ValueComposer
) : ValueComposer {

    override fun getValue(event: EventFromDbModel, config: ReportRequestConfig): ByteArray {
        return if (event.isLegacy()) {
            legacyValueComposer.getValue(event, config)
        } else {
            valueComposer.getValue(event, config)
        }
    }

    private fun EventFromDbModel.isLegacy(): Boolean {
        return (this.valueProtocolVersion ?: 0) < 2
    }
}
