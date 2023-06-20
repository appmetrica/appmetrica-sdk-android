package io.appmetrica.analytics.assertions.matcher

import io.appmetrica.analytics.assertions.matcher.field.ClassField

class PermittedFieldsMatcher(private val permittedFields: Collection<String>) : FieldMatcher {

    override fun match(field: ClassField) =
        FieldMatcher.Result(
            permittedFields.isEmpty() || permittedFields.contains(field.name),
            "Field does not match permitted fields. Permitted fields: permittedFields"
        )
}
