package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto

class DbLocationModelConverter(
    private val optionalBoolConverter: OptionalBoolConverter = OptionalBoolConverter()
) : Converter<DbLocationModel, DbProto.Location?> {

    override fun fromModel(value: DbLocationModel) = DbProto.Location().also { proto ->
        value.enabled?.let {
            proto.enabled = optionalBoolConverter.fromModel(it)
        }
        value.latitude?.let {
            proto.latitude = it
        }
        value.longitude?.let {
            proto.longitude = it
        }
        value.timestamp?.let {
            proto.timestamp = it
        }
        value.precision?.let {
            proto.precision = it
        }
        value.direction?.let {
            proto.direction = it
        }
        value.speed?.let {
            proto.speed = it
        }
        value.altitude?.let {
            proto.altitude = it
        }
        value.provider?.let {
            proto.provider = it
        }
        value.originalProvider?.let {
            proto.originalProvider = it
        }
    }

    override fun toModel(value: DbProto.Location?): DbLocationModel {
        if (value == null) {
            return DbLocationModel(
                enabled = null,
                latitude = null,
                longitude = null,
                timestamp = null,
                precision = null,
                direction = null,
                speed = null,
                altitude = null,
                provider = null,
                originalProvider = null,
            )
        }
        val defaultModel = DbProto.Location()
        return DbLocationModel(
            enabled = optionalBoolConverter.toModel(value.enabled),
            latitude = value.latitude.takeIf { it != defaultModel.latitude },
            longitude = value.longitude.takeIf { it != defaultModel.longitude },
            timestamp = value.timestamp.takeIf { it != defaultModel.timestamp },
            precision = value.precision.takeIf { it != defaultModel.precision },
            direction = value.direction.takeIf { it != defaultModel.direction },
            speed = value.speed.takeIf { it != defaultModel.speed },
            altitude = value.altitude.takeIf { it != defaultModel.altitude },
            provider = value.provider.takeIf { it != defaultModel.provider },
            originalProvider = value.originalProvider.takeIf { it != defaultModel.originalProvider },
        )
    }
}
