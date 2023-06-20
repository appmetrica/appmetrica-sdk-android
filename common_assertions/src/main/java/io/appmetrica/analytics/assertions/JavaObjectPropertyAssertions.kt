package io.appmetrica.analytics.assertions

import io.appmetrica.analytics.assertions.matcher.field.JavaField
import java.lang.reflect.Field

open class JavaObjectPropertyAssertions<T : Any>(actual: T?) : BaseObjectPropertyAssertions<T>(actual) {

    override fun <F> getValueOfField(fieldName: String): F? {
        try {
            val field = allFields[fieldName] as? JavaField
            if (field == null) {
                softAssertions.fail("Actual object does not contain field \"$fieldName\"")
                return null
            }
            if (doesFieldMatch(field)) {
                return getFieldValueReflective(field.field)
            }
        } catch (e: IllegalAccessException) {
            softAssertions.fail("Field \"$fieldName\" is not accessible with cause $e")
        }
        return null
    }

    protected open fun <F> getFieldValueReflective(field: Field): F {
        return field.get(actual) as F
    }
}
