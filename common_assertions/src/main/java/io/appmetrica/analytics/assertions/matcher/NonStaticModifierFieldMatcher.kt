package io.appmetrica.analytics.assertions.matcher

import io.appmetrica.analytics.assertions.matcher.field.ClassField

class NonStaticModifierFieldMatcher : FieldMatcher {

    override fun match(field: ClassField) =
        FieldMatcher.Result(
            field.isStatic() == false,
            "Field is static"
        )
}
