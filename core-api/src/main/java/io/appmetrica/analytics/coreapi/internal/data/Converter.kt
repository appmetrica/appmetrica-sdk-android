package io.appmetrica.analytics.coreapi.internal.data

interface Converter<S, P> {

    fun fromModel(value: S): P
    fun toModel(value: P): S
}
