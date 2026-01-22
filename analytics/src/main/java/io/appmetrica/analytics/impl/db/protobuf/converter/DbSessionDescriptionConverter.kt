package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto

internal class DbSessionDescriptionConverter(
    private val optionalBoolConverter: OptionalBoolConverter = OptionalBoolConverter()
) : ProtobufConverter<DbSessionModel.Description, DbProto.SessionDescription> {

    override fun fromModel(value: DbSessionModel.Description) = DbProto.SessionDescription().also { proto ->
        value.startTime?.let {
            proto.startTime = it
        }
        value.serverTimeOffset?.let {
            proto.serverTimeOffset = it
        }
        value.obtainedBeforeFirstSynchronization?.let {
            proto.obtainedBeforeFirstSynchronization = optionalBoolConverter.fromModel(it)
        }
    }

    override fun toModel(value: DbProto.SessionDescription): DbSessionModel.Description {
        val defaultModel = DbProto.SessionDescription()
        return DbSessionModel.Description(
            value.startTime.takeIf { it != defaultModel.startTime },
            value.serverTimeOffset.takeIf { it != defaultModel.serverTimeOffset },
            optionalBoolConverter.toModel(value.obtainedBeforeFirstSynchronization)
        )
    }
}
