package io.appmetrica.analytics.impl.db.protobuf.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.protobuf.client.DbProto

class OptionalBoolConverter : Converter<Boolean?, Int> {

    override fun fromModel(value: Boolean?): Int = when (value) {
        null -> DbProto.Utils.OPTIONAL_BOOL_UNDEFINED
        true -> DbProto.Utils.OPTIONAL_BOOL_TRUE
        false -> DbProto.Utils.OPTIONAL_BOOL_FALSE
    }

    override fun toModel(value: Int): Boolean? = when (value) {
        DbProto.Utils.OPTIONAL_BOOL_UNDEFINED -> null
        DbProto.Utils.OPTIONAL_BOOL_TRUE -> true
        DbProto.Utils.OPTIONAL_BOOL_FALSE -> false
        else -> null
    }
}
