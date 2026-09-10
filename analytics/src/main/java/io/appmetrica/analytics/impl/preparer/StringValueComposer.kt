package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.request.ReportRequestConfig

internal class StringValueComposer : ValueComposer {

    override fun getValue(event: EventFromDbModel, config: ReportRequestConfig): ByteArray {
        return event.foldValue(
            onLegacy = StringUtils::getUTF8Bytes,
            onBytes = { it },
            onEmpty = { ByteArray(0) },
        )
    }
}
