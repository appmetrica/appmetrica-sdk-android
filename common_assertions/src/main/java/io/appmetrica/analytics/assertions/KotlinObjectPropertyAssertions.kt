package io.appmetrica.analytics.assertions

import io.appmetrica.analytics.assertions.matcher.field.KotlinField

class KotlinObjectPropertyAssertions<T : Any> internal constructor(actual: T) : BaseObjectPropertyAssertions<T>(actual) {

    override fun reInitializeFields() {
        allFields = HashMap()
        unverifiedFields = HashMap()
        val matcher = createFieldMatcher()
        val foundFields = Utils.getFieldsFromKotlinClass(
            actual, includingParents, declaredAccessibleFields, privateFields
        )
        for (field in foundFields) {
            allFields[field.name] = field
            if (matcher.match(field).matches) {
                unverifiedFields[field.name] = field
            }
        }
    }

    override fun <F> getValueOfField(fieldName: String): F? {
        try {
            val field = allFields[fieldName] as? KotlinField
            if (field == null) {
                softAssertions.fail<Any>("Actual object does not contain field \"$fieldName\"")
                return null
            }
            if (doesFieldMatch(field)) {
                return field.field.getter.call(actual) as F
            }
        } catch (e: IllegalAccessException) {
            softAssertions.fail<Any>("Field \"$fieldName\" is not accessible with cause $e")
        }
        return null
    }
}
