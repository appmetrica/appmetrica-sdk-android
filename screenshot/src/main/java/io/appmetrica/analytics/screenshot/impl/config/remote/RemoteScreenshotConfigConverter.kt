package io.appmetrica.analytics.screenshot.impl.config.remote

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.converter.RemoteScreenshotConfigProtoConverter
import io.appmetrica.analytics.screenshot.internal.config.RemoteScreenshotConfig

internal class RemoteScreenshotConfigConverter(
    private val protoConverter: RemoteScreenshotConfigProtoConverter = RemoteScreenshotConfigProtoConverter()
) : Converter<RemoteScreenshotConfig, ByteArray> {

    private val tag = "[RemoteScreenshotConfigConverter]"

    override fun fromModel(value: RemoteScreenshotConfig): ByteArray {
        return MessageNano.toByteArray(protoConverter.fromModel(value))
    }

    override fun toModel(value: ByteArray): RemoteScreenshotConfig {
        val proto = try {
            RemoteScreenshotConfigProto.parseFrom(value)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            RemoteScreenshotConfigProto()
        }
        return protoConverter.toModel(proto)
    }
}
