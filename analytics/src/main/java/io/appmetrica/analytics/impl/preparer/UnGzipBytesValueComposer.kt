package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.coreutils.internal.io.Base64Utils
import io.appmetrica.analytics.coreutils.internal.io.GZIPUtils
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class UnGzipBytesValueComposer : ValueComposer {

    private val tag = "[UnGzipBytesValueComposer]"

    override fun getValue(event: EventFromDbModel, config: ReportRequestConfig): ByteArray {
        return try {
            event.foldValue(
                onLegacy = Base64Utils::decompressBase64GzipAsBytes,
                onBytes = GZIPUtils::unGzipBytes,
                onEmpty = { ByteArray(0) },
            ) ?: ByteArray(0)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            ByteArray(0)
        }
    }
}
