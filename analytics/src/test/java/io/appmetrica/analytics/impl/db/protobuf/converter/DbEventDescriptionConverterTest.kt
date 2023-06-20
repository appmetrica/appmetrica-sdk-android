package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DbEventDescriptionConverterTest : CommonTest() {

    private val customType = 42
    private val name = "name string"
    private val value = "value string"
    private val numberOfType = 4242L
    private val locationInfo: DbLocationModel = mock()
    private val errorEnvironment = "error environment string"
    private val appEnvironment = "app environment sting"
    private val appEnvironmentRevision = 123L
    private val truncated = 321
    private val connectionType = 234
    private val cellularConnectionType = "cellular connection type string"
    private val encryptingMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER
    private val profileId = "profile id string"
    private val firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE
    private val source = EventSource.JS
    private val attributionIdChanged = true
    private val openId = 31
    private val extras = "extras string".toByteArray()

    private val attributionIdChangedProto = DbProto.Utils.OPTIONAL_BOOL_TRUE
    private val attributionIdChangedProtoFromNull = DbProto.Utils.OPTIONAL_BOOL_UNDEFINED
    private val locationInfoProto: DbProto.Location = mock()

    private val optionalBoolConverter: OptionalBoolConverter = mock {
        on { toModel(attributionIdChangedProto) } doReturn attributionIdChanged
        on { toModel(-1) } doReturn null
        on { fromModel(attributionIdChanged) } doReturn attributionIdChangedProto
        on { fromModel(null) } doReturn attributionIdChangedProtoFromNull
    }
    private val locationConverter: DbLocationModelConverter = mock {
        on { toModel(locationInfoProto) } doReturn locationInfo
        on { fromModel(locationInfo) } doReturn locationInfoProto
    }

    private val converter = DbEventDescriptionConverter(
        optionalBoolConverter,
        locationConverter
    )

    @Test
    fun fromModel() {
        val model = DbEventModel.Description(
            customType,
            name,
            value,
            numberOfType,
            locationInfo,
            errorEnvironment,
            appEnvironment,
            appEnvironmentRevision,
            truncated,
            connectionType,
            cellularConnectionType,
            encryptingMode,
            profileId,
            firstOccurrenceStatus,
            source,
            attributionIdChanged,
            openId,
            extras
        )
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("customType", customType)
            .checkField("name", name)
            .checkField("value", value)
            .checkField("numberOfType", numberOfType)
            .checkField("locationInfo", locationInfoProto)
            .checkField("errorEnvironment", errorEnvironment)
            .checkField("appEnvironment", appEnvironment)
            .checkField("appEnvironmentRevision", appEnvironmentRevision)
            .checkField("truncated", truncated)
            .checkField("connectionType", connectionType)
            .checkField("cellularConnectionType", cellularConnectionType)
            .checkField("encryptingMode", encryptingMode.modeId)
            .checkField("profileId", profileId)
            .checkField("firstOccurrenceStatus", firstOccurrenceStatus.mStatusCode)
            .checkField("source", source.code)
            .checkField("attributionIdChanged", attributionIdChangedProto)
            .checkField("openId", openId)
            .checkField("extras", extras)
            .checkAll()
    }

    @Test
    fun fromModelIfNullFields() {
        val model = DbEventModel.Description(null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null)
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("customType", -1)
            .checkField("name", "")
            .checkField("value", "")
            .checkField("numberOfType", -1L)
            .checkFieldIsNull("locationInfo")
            .checkField("errorEnvironment", "")
            .checkField("appEnvironment", "")
            .checkField("appEnvironmentRevision", -1L)
            .checkField("truncated", -1)
            .checkField("connectionType", -1)
            .checkField("cellularConnectionType", "")
            .checkField("encryptingMode", -1)
            .checkField("profileId", "")
            .checkField("firstOccurrenceStatus", -1)
            .checkField("source", -1)
            .checkField("attributionIdChanged", attributionIdChangedProtoFromNull)
            .checkField("openId", -1)
            .checkField("extras", "".toByteArray())
            .checkAll()
    }

    @Test
    fun toModel() {
        val proto = DbProto.EventDescription().also {
            it.customType = customType
            it.name = name
            it.value = value
            it.numberOfType = numberOfType
            it.locationInfo = locationInfoProto
            it.errorEnvironment = errorEnvironment
            it.appEnvironment = appEnvironment
            it.appEnvironmentRevision = appEnvironmentRevision
            it.truncated = truncated
            it.connectionType = connectionType
            it.cellularConnectionType = cellularConnectionType
            it.encryptingMode = encryptingMode.modeId
            it.profileId = profileId
            it.firstOccurrenceStatus = firstOccurrenceStatus.mStatusCode
            it.source = source.code
            it.attributionIdChanged = attributionIdChangedProto
            it.openId = openId
            it.extras = extras
        }
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkField("customType", customType)
            .checkField("name", name)
            .checkField("value", value)
            .checkField("numberOfType", numberOfType)
            .checkField("locationInfo", locationInfo)
            .checkField("errorEnvironment", errorEnvironment)
            .checkField("appEnvironment", appEnvironment)
            .checkField("appEnvironmentRevision", appEnvironmentRevision)
            .checkField("truncated", truncated)
            .checkField("connectionType", connectionType)
            .checkField("cellularConnectionType", cellularConnectionType)
            .checkField("encryptingMode", encryptingMode)
            .checkField("profileId", profileId)
            .checkField("firstOccurrenceStatus", firstOccurrenceStatus)
            .checkField("source", source)
            .checkField("attributionIdChanged", attributionIdChanged)
            .checkField("openId", openId)
            .checkField("extras", extras)
            .checkAll()
    }

    @Test
    fun toModelIfFieldsAreDefault() {
        val proto = DbProto.EventDescription()
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkFieldsAreNull(
                "customType",
                "name",
                "value",
                "numberOfType",
                "locationInfo",
                "errorEnvironment",
                "appEnvironment",
                "appEnvironmentRevision",
                "truncated",
                "connectionType",
                "cellularConnectionType",
                "encryptingMode",
                "profileId",
                "firstOccurrenceStatus",
                "source",
                "attributionIdChanged",
                "openId",
                "extras"
            ).checkAll()
    }

    @Test
    fun toModelIfFieldsAreDefaultExplicitly() {
        val proto = DbProto.EventDescription().also {
            it.customType = -1
            it.name = ""
            it.value = ""
            it.numberOfType = -1
            it.locationInfo = null
            it.errorEnvironment = ""
            it.appEnvironment = ""
            it.appEnvironmentRevision = -1
            it.truncated = -1
            it.connectionType = -1
            it.cellularConnectionType = ""
            it.encryptingMode = -1
            it.profileId = ""
            it.firstOccurrenceStatus = -1
            it.source = -1
            it.attributionIdChanged = -1
            it.openId = -1
            it.extras = "".toByteArray()
        }
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkFieldsAreNull(
                "customType",
                "name",
                "value",
                "numberOfType",
                "locationInfo",
                "errorEnvironment",
                "appEnvironment",
                "appEnvironmentRevision",
                "truncated",
                "connectionType",
                "cellularConnectionType",
                "encryptingMode",
                "profileId",
                "firstOccurrenceStatus",
                "source",
                "attributionIdChanged",
                "openId",
                "extras"
            ).checkAll()
    }
}

