package io.appmetrica.analytics.assertions.matcher

import io.appmetrica.analytics.assertions.matcher.field.ClassField

class FinalModifierFieldMatcher(private val finalFieldsOnly: Boolean) : FieldMatcher {

    override fun match(field: ClassField) =
        FieldMatcher.Result(
            finalFieldsOnly == false || field.isFinal(),
            "Field should ${if (finalFieldsOnly) "be" else "not be"} final"
        )
}
