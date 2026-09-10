package io.appmetrica.analytics.impl.preparer

import android.util.Base64
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class BytesValueComposer : ValueComposer {

    private val tag = "[BytesValueComposer]"

    override fun getValue(event: EventFromDbModel, config: ReportRequestConfig): ByteArray {
        return event.foldValue(
            onLegacy = { legacy ->
                try {
                    Base64.decode(legacy, Base64.DEFAULT)
                } catch (e: Throwable) {
                    DebugLogger.error(
                        tag,
                        e,
                        "Something went wrong while decoding base 64 event value."
                    )
                    ByteArray(0)
                }
            },
            onBytes = { it },
            onEmpty = { ByteArray(0) },
        )
    }
}
