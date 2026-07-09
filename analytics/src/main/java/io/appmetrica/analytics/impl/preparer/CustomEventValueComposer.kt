package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider

internal class CustomEventValueComposer(
    val valueComposer: ValueComposer,
    val legacyValueComposer: ValueComposer,
    val eventEncrypterProvider: EventEncrypterProvider
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
