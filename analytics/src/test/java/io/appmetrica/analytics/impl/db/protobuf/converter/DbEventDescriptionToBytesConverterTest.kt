package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import org.mockito.kotlin.whenever

class DbEventDescriptionToBytesConverterTest : CommonTest() {

    @get:Rule
    val messageNanoRule = MockedStaticRule(MessageNano::class.java)
    @get:Rule
    val eventDescriptionProtoRule = MockedStaticRule(DbProto.EventDescription::class.java)

    private val model: DbEventModel.Description = mock()
    private val emptyModel: DbEventModel.Description = mock()
    private val proto: DbProto.EventDescription = mock()
    private val protoBytes = "some bytes".toByteArray()

    private val descriptionConverter: DbEventDescriptionConverter = mock {
        on { toModel(proto) } doReturn model
        on { toModel(refEq(DbProto.EventDescription())) } doReturn emptyModel
        on { fromModel(model) } doReturn proto
    }
    private val converter = DbEventDescriptionToBytesConverter(descriptionConverter)

    @Test
    fun fromModel() {
        whenever(MessageNano.toByteArray(proto)).thenReturn(protoBytes)

        assertThat(converter.fromModel(model)).isEqualTo(protoBytes)
    }

    @Test
    fun toModel() {
        whenever(DbProto.EventDescription.parseFrom(protoBytes)).thenReturn(proto)

        assertThat(converter.toModel(protoBytes)).isEqualTo(model)
    }

    @Test
    fun toModelIfWrongBytes() {
        whenever(DbProto.EventDescription.parseFrom(protoBytes))
            .thenThrow(InvalidProtocolBufferNanoException("description"))

        val model = converter.toModel(protoBytes)

        assertThat(model).isEqualTo(emptyModel)
    }

    @Test
    fun toModelIfNullBytes() {
        whenever(DbProto.EventDescription.parseFrom(protoBytes))
            .thenReturn(null)

        val model = converter.toModel(null)

        assertThat(model).isEqualTo(emptyModel)
    }
}
