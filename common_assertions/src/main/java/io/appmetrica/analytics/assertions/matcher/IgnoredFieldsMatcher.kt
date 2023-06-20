package io.appmetrica.analytics.assertions.matcher

import io.appmetrica.analytics.assertions.matcher.field.ClassField

class IgnoredFieldsMatcher(private val ignoredFields: Collection<String>) : FieldMatcher {

    override fun match(field: ClassField) =
        FieldMatcher.Result(
            field.name !in ignoredFields,
            "Field is in ignored fields list. Ignored fields: $ignoredFields"
        )
}
