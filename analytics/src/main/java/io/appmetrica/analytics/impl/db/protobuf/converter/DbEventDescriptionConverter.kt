package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode

internal class DbEventDescriptionConverter(
    private val optionalBoolConverter: OptionalBoolConverter = OptionalBoolConverter(),
    private val locationConverter: DbLocationModelConverter = DbLocationModelConverter()
) : ProtobufConverter<DbEventModel.Description, DbProto.EventDescription> {

    override fun fromModel(value: DbEventModel.Description) = DbProto.EventDescription().also { proto ->
        value.customType?.let {
            proto.customType = it
        }
        value.name?.let {
            proto.name = StringUtils.correctIllFormedString(it)
        }
        value.value?.let {
            proto.value = StringUtils.correctIllFormedString(it)
        }
        value.numberOfType?.let {
            proto.numberOfType = it
        }
        value.locationInfo?.let {
            proto.locationInfo = locationConverter.fromModel(it)
        }
        value.errorEnvironment?.let {
            proto.errorEnvironment = it
        }
        value.appEnvironment?.let {
            proto.appEnvironment = it
        }
        value.appEnvironmentRevision?.let {
            proto.appEnvironmentRevision = it
        }
        value.truncated?.let {
            proto.truncated = it
        }
        value.connectionType?.let {
            proto.connectionType = it
        }
        value.cellularConnectionType?.let {
            proto.cellularConnectionType = it
        }
        value.encryptingMode?.let {
            proto.encryptingMode = it.modeId
        }
        value.profileId?.let {
            proto.profileId = it
        }
        value.firstOccurrenceStatus?.let {
            proto.firstOccurrenceStatus = it.mStatusCode
        }
        value.source?.let {
            proto.source = it.code
        }
        value.attributionIdChanged?.let {
            proto.attributionIdChanged = optionalBoolConverter.fromModel(it)
        }
        value.openId?.let {
            proto.openId = it
        }
        value.extras?.let {
            proto.extras = it
        }
    }

    override fun toModel(value: DbProto.EventDescription): DbEventModel.Description {
        val defaultModel = DbProto.EventDescription()
        return DbEventModel.Description(
            value.customType.takeIf { it != defaultModel.customType },
            value.name.takeIf { it != defaultModel.name },
            value.value.takeIf { it != defaultModel.value },
            value.numberOfType.takeIf { it != defaultModel.numberOfType },
            locationConverter.toModel(value.locationInfo),
            value.errorEnvironment.takeIf { it != defaultModel.errorEnvironment },
            value.appEnvironment.takeIf { it != defaultModel.appEnvironment },
            value.appEnvironmentRevision.takeIf { it != defaultModel.appEnvironmentRevision },
            value.truncated.takeIf { it != defaultModel.truncated },
            value.connectionType.takeIf { it != defaultModel.connectionType },
            value.cellularConnectionType.takeIf { it != defaultModel.cellularConnectionType },
            value.encryptingMode.takeIf { it != defaultModel.encryptingMode }
                ?.let { EventEncryptionMode.valueOf(it) },
            value.profileId.takeIf { it != defaultModel.profileId },
            value.firstOccurrenceStatus.takeIf { it != defaultModel.firstOccurrenceStatus }
                ?.let { FirstOccurrenceStatus.fromStatusCode(it) },
            value.source.takeIf { it != defaultModel.source }
                ?.let { EventSource.fromCode(it) },
            optionalBoolConverter.toModel(value.attributionIdChanged),
            value.openId.takeIf { it != defaultModel.openId },
            value.extras.takeIf { !it.contentEquals(defaultModel.extras) },
        )
    }
}
