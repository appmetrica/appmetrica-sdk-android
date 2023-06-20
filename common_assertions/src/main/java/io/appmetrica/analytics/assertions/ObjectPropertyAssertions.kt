package io.appmetrica.analytics.assertions

import java.math.BigDecimal
import java.util.function.Consumer
import java.util.function.Predicate

interface ObjectPropertyAssertions<T : Any> {

    val actual: T

    // region checkFieldNonNull
    fun checkFieldNonNull(fieldName: String): ObjectPropertyAssertions<T>

    fun checkFieldNonNull(fieldName: String, getterName: String): ObjectPropertyAssertions<T>

    fun checkFieldsNonNull(vararg names: String): ObjectPropertyAssertions<T>
    // endregion

    // region checkFieldIsNull
    fun checkFieldIsNull(fieldName: String): ObjectPropertyAssertions<T>

    fun checkFieldIsNull(fieldName: String, getterName: String): ObjectPropertyAssertions<T>

    fun checkFieldsAreNull(vararg names: String): ObjectPropertyAssertions<T>
    // endregion

    // region checkField
    fun checkField(fieldName: String, expected: Any?): ObjectPropertyAssertions<T>

    fun checkField(fieldName: String, getterName: String, expected: Any?): ObjectPropertyAssertions<T>

    fun checkFieldComparingFieldByField(fieldName: String, expected: Any?): ObjectPropertyAssertions<T>

    fun checkFieldComparingFieldByField(fieldName: String, getterName: String, expected: Any?): ObjectPropertyAssertions<T>

    fun checkFieldComparingFieldByFieldRecursively(fieldName: String, expected: Any?): ObjectPropertyAssertions<T>

    fun checkFieldComparingFieldByFieldRecursively(fieldName: String, getterName: String, expected: Any?): ObjectPropertyAssertions<T>

    fun <S : Any> checkFieldRecursively(fieldName: String, nestedChecker: Consumer<ObjectPropertyAssertions<S>>?): ObjectPropertyAssertions<T>

    fun <S : Any> checkFieldRecursively(fieldName: String, getterName: String, nestedChecker: Consumer<ObjectPropertyAssertions<S>>?): ObjectPropertyAssertions<T>

    fun <S : Any> checkFieldMatchPredicate(fieldName: String, predicate: Predicate<S>): ObjectPropertyAssertions<T>

    fun <S : Any> checkFieldMatchPredicate(fieldName: String, getterName: String, predicate: Predicate<S>): ObjectPropertyAssertions<T>
    // endregion

    // region primitives
    fun checkIntFieldIsZero(fieldName: String): ObjectPropertyAssertions<T>

    fun checkIntFieldIsZero(fieldName: String, getterName: String): ObjectPropertyAssertions<T>

    fun checkFloatFieldIsZero(fieldName: String): ObjectPropertyAssertions<T>

    fun checkFloatFieldIsZero(fieldName: String, getterName: String): ObjectPropertyAssertions<T>

    fun checkFloatField(fieldName: String, expected: Float, accuracy: Float): ObjectPropertyAssertions<T>

    fun checkFloatField(fieldName: String, getterName: String, expected: Float, accuracy: Float): ObjectPropertyAssertions<T>

    fun checkDecimalField(fieldName: String, getterName: String, expected: BigDecimal?, percentage: Double): ObjectPropertyAssertions<T>
    // endregion

    // region checkField iterable
    fun <P> checkField(fieldName: String, expected: Iterable<P>?, sameOrder: Boolean): ObjectPropertyAssertions<T>

    fun <P> checkField(fieldName: String, expected: Array<P>?): ObjectPropertyAssertions<T>

    fun <P> checkField(fieldName: String, expected: Array<P>?, sameOrder: Boolean): ObjectPropertyAssertions<T>
    // endregion

    // region checkFieldIsInstanceOf
    fun checkFieldIsInstanceOf(fieldName: String, type: Class<*>): ObjectPropertyAssertions<T>

    fun checkFieldIsInstanceOf(fieldName: String, getterName: String, type: Class<*>): ObjectPropertyAssertions<T>
    // endregion

    // region settings
    fun withIgnoredFields(vararg ignoredFields: String): ObjectPropertyAssertions<T>

    fun withPermittedFields(vararg permittedFields: String): ObjectPropertyAssertions<T>

    fun withFinalFieldOnly(value: Boolean): ObjectPropertyAssertions<T>

    fun withDeclaredAccessibleFields(value: Boolean): ObjectPropertyAssertions<T>

    fun withPrivateFields(value: Boolean): ObjectPropertyAssertions<T>

    fun includingParents(value: Boolean): ObjectPropertyAssertions<T>
    // endregion

    // region common
    fun checkAll()

    fun getUnverifiedFieldsCount(): Int
    // endregion
}
