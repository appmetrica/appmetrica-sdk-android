package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException
import io.appmetrica.analytics.protobuf.nano.MessageNano

class DbSessionDescriptionToBytesConverter(
    private val dbSessionDescriptionConverter: DbSessionDescriptionConverter = DbSessionDescriptionConverter()
) : Converter<DbSessionModel.Description, ByteArray?> {

    private val tag = "[DbSessionDescriptionToBytesConverter]"

    override fun fromModel(value: DbSessionModel.Description): ByteArray = MessageNano.toByteArray(
        dbSessionDescriptionConverter.fromModel(value)
    )

    override fun toModel(value: ByteArray?): DbSessionModel.Description {
        val proto = try {
            value?.let {
                DbProto.SessionDescription.parseFrom(it)
            } ?: DbProto.SessionDescription()
        } catch (e: InvalidProtocolBufferNanoException) {
            YLogger.error(tag, e)
            DbProto.SessionDescription()
        }
        return dbSessionDescriptionConverter.toModel(proto)
    }
}
