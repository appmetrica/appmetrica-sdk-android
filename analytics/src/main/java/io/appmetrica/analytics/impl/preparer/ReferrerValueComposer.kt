package io.appmetrica.analytics.impl.preparer

import android.util.Base64
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.protobuf.backend.Referrer
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class ReferrerValueComposer : ValueComposer {

    private val tag = "[ReferrerValueComposer]"

    override fun getValue(event: EventFromDbModel, config: ReportRequestConfig): ByteArray {
        try {
            val encoded = event.foldValue(
                onLegacy = { legacy ->
                    if (StringUtils.isNullOrEmpty(legacy)) {
                        null
                    } else {
                        Base64.decode(legacy, Base64.DEFAULT)
                    }
                },
                onBytes = { bytes -> bytes.takeIf { it.isNotEmpty() } },
                onEmpty = { null },
            ) ?: return ByteArray(0)
            val info = ReferrerInfo.parseFrom(encoded) ?: return ByteArray(0)
            val referrer = Referrer().apply {
                this.referrer = info.installReferrer.toByteArray()
                clickTimestamp = info.referrerClickTimestampSeconds
                installBeginTimestamp = info.installBeginTimestampSeconds
                source = sourceToProto(info.source)
            }
            return MessageNano.toByteArray(referrer)
        } catch (e: Throwable) {
            DebugLogger.error(
                tag,
                e,
                "Something went wrong while serializing referrer event."
            )
        }
        return ByteArray(0)
    }

    private fun sourceToProto(source: ReferrerInfo.Source): Int = when (source) {
        ReferrerInfo.Source.GP -> Referrer.GP
        ReferrerInfo.Source.HMS -> Referrer.HMS
        ReferrerInfo.Source.RS -> Referrer.RS
        ReferrerInfo.Source.UNKNOWN -> Referrer.UNKNOWN
    }
}
