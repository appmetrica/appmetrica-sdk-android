package io.appmetrica.analytics.screenshot.impl.config.service

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.converter.ScreenshotConfigProtoConverter
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig

internal class ServiceSideScreenshotConfigConverter(
    private val protoConverter: ScreenshotConfigProtoConverter = ScreenshotConfigProtoConverter()
) : Converter<ServiceSideScreenshotConfig, ByteArray> {

    private val tag = "[ServiceSideScreenshotConfigConverter]"

    override fun fromModel(value: ServiceSideScreenshotConfig): ByteArray {
        return MessageNano.toByteArray(protoConverter.fromModel(value))
    }

    override fun toModel(value: ByteArray): ServiceSideScreenshotConfig {
        val proto = try {
            RemoteScreenshotConfigProto.parseFrom(value)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            RemoteScreenshotConfigProto()
        }
        return protoConverter.toModel(proto)
    }
}
