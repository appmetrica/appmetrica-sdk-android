package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class DbSessionDescriptionConverterTest : CommonTest() {

    private val startTime = 424242L
    private val serverTimeOffset = 42L
    private val obtainedBeforeFirstSynchronization = true
    private val obtainedBeforeFirstSynchronizationProto = DbProto.Utils.OPTIONAL_BOOL_TRUE

    private val converter = DbSessionDescriptionConverter()

    @Test
    fun fromModel() {
        val model = DbSessionModel.Description(
            startTime,
            serverTimeOffset,
            obtainedBeforeFirstSynchronization
        )
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("startTime", startTime)
            .checkField("serverTimeOffset", serverTimeOffset)
            .checkField("obtainedBeforeFirstSynchronization", obtainedBeforeFirstSynchronizationProto)
            .checkAll()
    }

    @Test
    fun fromModelIfNullFields() {
        val model = DbSessionModel.Description(null, null, null)
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkField("startTime", -1L)
            .checkField("serverTimeOffset", -1L)
            .checkField("obtainedBeforeFirstSynchronization", DbProto.Utils.OPTIONAL_BOOL_UNDEFINED)
            .checkAll()
    }

    @Test
    fun toModel() {
        val proto = DbProto.SessionDescription().also {
            it.startTime = startTime
            it.serverTimeOffset = serverTimeOffset
            it.obtainedBeforeFirstSynchronization = obtainedBeforeFirstSynchronizationProto
        }
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkField("startTime", startTime)
            .checkField("serverTimeOffset", serverTimeOffset)
            .checkField("obtainedBeforeFirstSynchronization", obtainedBeforeFirstSynchronization)
            .checkAll()
    }

    @Test
    fun toModelIfStartTimeIsDefault() {
        val proto = DbProto.SessionDescription()
        val model = DbSessionDescriptionConverter().toModel(proto)
        ObjectPropertyAssertions(model)
            .checkFieldsAreNull(
                "startTime",
                "serverTimeOffset",
                "obtainedBeforeFirstSynchronization"
            )
            .checkAll()
    }
}
