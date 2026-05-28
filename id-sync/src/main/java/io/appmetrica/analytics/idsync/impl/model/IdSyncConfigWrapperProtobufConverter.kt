package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.idsync.internal.IdSyncConfigWrapper
import io.appmetrica.analytics.idsync.internal.IdSyncConfigWrapper.Companion.toWrapper

internal class IdSyncConfigWrapperProtobufConverter(
    private val converter: IdSyncConfigToProtoBytesConverter,
) : Converter<IdSyncConfigWrapper, ByteArray> {

    override fun fromModel(value: IdSyncConfigWrapper): ByteArray = converter.fromModel(value.config)

    override fun toModel(value: ByteArray): IdSyncConfigWrapper = converter.toModel(value).toWrapper()
}
