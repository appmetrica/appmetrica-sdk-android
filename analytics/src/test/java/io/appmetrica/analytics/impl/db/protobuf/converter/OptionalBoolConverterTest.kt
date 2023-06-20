package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.impl.protobuf.client.DbProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class OptionalBoolConverterTest : CommonTest() {

    private val converter = OptionalBoolConverter()

    @Test
    fun toAndFromModel() {
        SoftAssertions().apply {
            assertThat(converter.fromModel(converter.toModel(DbProto.Utils.OPTIONAL_BOOL_UNDEFINED)))
                .isEqualTo(DbProto.Utils.OPTIONAL_BOOL_UNDEFINED)
            assertThat(converter.fromModel(converter.toModel(DbProto.Utils.OPTIONAL_BOOL_TRUE)))
                .isEqualTo(DbProto.Utils.OPTIONAL_BOOL_TRUE)
            assertThat(converter.fromModel(converter.toModel(DbProto.Utils.OPTIONAL_BOOL_FALSE)))
                .isEqualTo(DbProto.Utils.OPTIONAL_BOOL_FALSE)
        }.assertAll()
    }
}
