package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class DbSessionDescriptionToBytesConverterTest : CommonTest() {

    private val startTime = 424242L
    private val serverTimeOffset = 42L
    private val obtainedBeforeFirstSynchronization = true

    private val converter = DbSessionDescriptionToBytesConverter()

    @Test
    fun fromAndToModel() {
        val model = DbSessionModel.Description(
            startTime,
            serverTimeOffset,
            obtainedBeforeFirstSynchronization
        )
        val bytes = converter.fromModel(model)
        val rebuildModel = converter.toModel(bytes)
        assertThat(rebuildModel).isEqualToComparingFieldByField(model)
    }

    @Test
    fun toModelIfWrongBytes() {
        val bytes = "some string".toByteArray()
        val model = converter.toModel(bytes)
        val expected = DbSessionDescriptionConverter().toModel(DbProto.SessionDescription())
        assertThat(model).isEqualToComparingFieldByField(expected)
    }

    @Test
    fun toModelIfNullBytes() {
        val model = converter.toModel(null)
        val expected = DbSessionDescriptionConverter().toModel(DbProto.SessionDescription())
        assertThat(model).isEqualToComparingFieldByField(expected)
    }
}
