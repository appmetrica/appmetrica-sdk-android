package io.appmetrica.analytics.assertions.matcher

import io.appmetrica.analytics.assertions.matcher.field.ClassField

class CompositeFieldMatcher(private val matchers: List<FieldMatcher>) : FieldMatcher {

    override fun match(field: ClassField): FieldMatcher.Result {
        return matchers
            .map { it.match(field) }
            .firstOrNull { it.matches == false } ?: FieldMatcher.Result(true, null)
    }
}
