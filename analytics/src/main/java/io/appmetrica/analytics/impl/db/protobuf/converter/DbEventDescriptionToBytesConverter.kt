package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException
import io.appmetrica.analytics.protobuf.nano.MessageNano

class DbEventDescriptionToBytesConverter(
    private val descriptionConverter: DbEventDescriptionConverter = DbEventDescriptionConverter()
) : Converter<DbEventModel.Description, ByteArray?> {

    private val tag = "[DbEventDescriptionToBytesConverter]"

    override fun fromModel(value: DbEventModel.Description): ByteArray = MessageNano.toByteArray(
        descriptionConverter.fromModel(value)
    )

    override fun toModel(value: ByteArray?): DbEventModel.Description {
        val proto = try {
            value?.let {
                DbProto.EventDescription.parseFrom(it)
            } ?: DbProto.EventDescription()
        } catch (e: InvalidProtocolBufferNanoException) {
            YLogger.error(tag, e)
            DbProto.EventDescription()
        }
        return descriptionConverter.toModel(proto)
    }
}
