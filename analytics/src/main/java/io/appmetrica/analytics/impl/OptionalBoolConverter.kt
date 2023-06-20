package io.appmetrica.analytics.impl

internal open class OptionalBoolConverter(
    private val undefinedValue: Int,
    private val falseValue: Int,
    private val trueValue: Int,
) {

    fun toProto(value: Boolean?): Int = when (value) {
        null -> undefinedValue
        false -> falseValue
        true -> trueValue
    }

    fun toModel(value: Int): Boolean? = when (value) {
        falseValue -> false
        trueValue -> true
        else -> null
    }
}
