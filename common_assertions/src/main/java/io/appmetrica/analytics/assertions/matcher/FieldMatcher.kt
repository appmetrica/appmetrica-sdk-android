package io.appmetrica.analytics.assertions.matcher

import io.appmetrica.analytics.assertions.matcher.field.ClassField

interface FieldMatcher {

    data class Result(
        val matches: Boolean,
        val noMatchCause: String?
    )

    fun match(field: ClassField): Result
}
