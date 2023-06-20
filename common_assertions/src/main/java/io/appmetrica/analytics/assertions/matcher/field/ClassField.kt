package io.appmetrica.analytics.assertions.matcher.field

interface ClassField {

    val name: String

    fun isStatic(): Boolean

    fun isFinal(): Boolean
}
