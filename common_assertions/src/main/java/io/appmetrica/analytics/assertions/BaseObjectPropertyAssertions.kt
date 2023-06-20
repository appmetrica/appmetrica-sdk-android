package io.appmetrica.analytics.assertions

import io.appmetrica.analytics.assertions.Utils.getFieldsFromClass
import io.appmetrica.analytics.assertions.equality.ComparingFieldByFieldEqualityStrategy
import io.appmetrica.analytics.assertions.equality.ComparingFieldByFieldRecursivelyEqualityStrategy
import io.appmetrica.analytics.assertions.equality.EqualityStrategy
import io.appmetrica.analytics.assertions.equality.SimpleEqualityStrategy
import io.appmetrica.analytics.assertions.matcher.CompositeFieldMatcher
import io.appmetrica.analytics.assertions.matcher.FieldMatcher
import io.appmetrica.analytics.assertions.matcher.FinalModifierFieldMatcher
import io.appmetrica.analytics.assertions.matcher.IgnoredFieldsMatcher
import io.appmetrica.analytics.assertions.matcher.NonStaticModifierFieldMatcher
import io.appmetrica.analytics.assertions.matcher.PermittedFieldsMatcher
import io.appmetrica.analytics.assertions.matcher.field.ClassField
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.math.BigDecimal
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.math.abs

abstract class BaseObjectPropertyAssertions<T : Any>(value: T?) : ObjectPropertyAssertions<T> {

    init {
        assertThat(value).isNotNull
    }

    override val actual: T = value!!

    protected var unverifiedFields = mutableMapOf<String, ClassField>()
    protected var allFields = mutableMapOf<String, ClassField>()

    protected var finalFieldsOnly = true
    protected var declaredAccessibleFields = false
    protected var privateFields = false
    protected var includingParents = false

    protected var ignoredFields = mutableSetOf<String>()
    protected var permittedFields = mutableSetOf<String>()
    protected var softAssertions = SoftAssertions()

    protected val simpleEqualityStrategy: EqualityStrategy = SimpleEqualityStrategy()
    protected val comparingFieldByFieldEqualityStrategy: EqualityStrategy = ComparingFieldByFieldEqualityStrategy()
    protected val comparingFieldByFieldRecursivelyEqualityStrategy: EqualityStrategy =
        ComparingFieldByFieldRecursivelyEqualityStrategy()

    init {
        reInitializeFields()
    }

    // region checkFieldNonNull
    override fun checkFieldNonNull(
        fieldName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\"")
            .isNotNull
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFieldNonNull(
        fieldName: String,
        getterName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName, getterName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
            .isNotNull
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFieldsNonNull(
        vararg names: String
    ): ObjectPropertyAssertions<T> {
        for (name in names) {
            checkFieldNonNull(name)
        }
        return this
    }
    // endregion

    // region checkFieldIsNull
    override fun checkFieldIsNull(
        fieldName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\"")
            .isNull()
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFieldIsNull(
        fieldName: String,
        getterName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName, getterName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
            .isNull()
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFieldsAreNull(
        vararg names: String
    ): ObjectPropertyAssertions<T> {
        for (name in names) {
            checkFieldIsNull(name)
        }
        return this
    }
    // endregion

    // region checkField
    override fun checkField(
        fieldName: String,
        expected: Any?
    ): ObjectPropertyAssertions<T> {
        return checkFieldUsingEqualityStrategy(fieldName, expected, simpleEqualityStrategy)
    }

    override fun checkField(
        fieldName: String,
        getterName: String,
        expected: Any?
    ): ObjectPropertyAssertions<T> {
        return checkFieldUsingEqualityStrategy(fieldName, getterName, expected, simpleEqualityStrategy)
    }

    override fun checkFieldComparingFieldByField(
        fieldName: String,
        expected: Any?
    ): ObjectPropertyAssertions<T> {
        return checkFieldUsingEqualityStrategy(fieldName, expected, comparingFieldByFieldEqualityStrategy)
    }

    override fun checkFieldComparingFieldByField(
        fieldName: String,
        getterName: String,
        expected: Any?
    ): ObjectPropertyAssertions<T> {
        return checkFieldUsingEqualityStrategy(fieldName, getterName, expected, comparingFieldByFieldEqualityStrategy)
    }

    override fun checkFieldComparingFieldByFieldRecursively(
        fieldName: String,
        expected: Any?
    ): ObjectPropertyAssertions<T> {
        return checkFieldUsingEqualityStrategy(
            fieldName,
            expected,
            comparingFieldByFieldRecursivelyEqualityStrategy
        )
    }

    override fun checkFieldComparingFieldByFieldRecursively(
        fieldName: String,
        getterName: String,
        expected: Any?
    ): ObjectPropertyAssertions<T> {
        return checkFieldUsingEqualityStrategy(
            fieldName,
            getterName,
            expected,
            comparingFieldByFieldRecursivelyEqualityStrategy
        )
    }

    protected fun checkFieldUsingEqualityStrategy(
        fieldName: String,
        expected: Any?,
        equalityStrategy: EqualityStrategy
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName)
        val assertValue = softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\"")
        if (expected == null) {
            assertValue.isNull()
        } else {
            equalityStrategy.isEqual(assertValue, expected)
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    protected fun checkFieldUsingEqualityStrategy(
        fieldName: String,
        getterName: String,
        expected: Any?,
        equalityStrategy: EqualityStrategy
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName, getterName)
        val assertValue = softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
        if (expected == null) {
            assertValue.isNull()
        } else {
            equalityStrategy.isEqual(assertValue, expected)
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun <S : Any> checkFieldRecursively(
        fieldName: String,
        nestedChecker: Consumer<ObjectPropertyAssertions<S>>?
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<S?>(fieldName)
        return checkFieldRecursivelyInternal(fieldName, "Field \"$fieldName\"", value, nestedChecker)
    }

    override fun <S : Any> checkFieldRecursively(
        fieldName: String,
        getterName: String,
        nestedChecker: Consumer<ObjectPropertyAssertions<S>>?
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<S?>(fieldName, getterName)
        return checkFieldRecursivelyInternal(fieldName, "Field \"$fieldName\" via getter \"$getterName\"", value, nestedChecker)
    }

    private fun <S: Any> checkFieldRecursivelyInternal(
        fieldName: String,
        description: String,
        value: S?,
        nestedChecker: Consumer<ObjectPropertyAssertions<S>>?
    ): BaseObjectPropertyAssertions<T> {
        if (nestedChecker == null) {
            softAssertions.assertThat(value)
                .`as`(description)
                .isNull()
        } else {
            val nestedAssertions = createNestedAssertions(value)
            nestedChecker.accept(nestedAssertions)
            nestedAssertions.checkAll()
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun <S : Any> checkFieldMatchPredicate(
        fieldName: String,
        predicate: Predicate<S>
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<S>(fieldName)
        when {
            value == null -> softAssertions.fail("Expected field \"$fieldName\" to be nonnull")
            predicate.test(value) == false -> softAssertions.fail("Expected field \"$fieldName\" to match predicate")
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun <S : Any> checkFieldMatchPredicate(
        fieldName: String,
        getterName: String,
        predicate: Predicate<S>
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<S>(fieldName, getterName)
        when {
            value == null -> softAssertions.fail("Expected field \"$fieldName\" to be nonnull")
            predicate.test(value) == false -> softAssertions.fail("Expected field \"$fieldName\" with getter \"$getterName\" to match predicate")
        }
        unverifiedFields.remove(fieldName)
        return this
    }
    // endregion

    // region primitives
    override fun checkIntFieldIsZero(
        fieldName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Int>(fieldName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\"")
            .isZero
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkIntFieldIsZero(
        fieldName: String,
        getterName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Int>(fieldName, getterName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
            .isZero
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFloatFieldIsZero(
        fieldName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Float>(fieldName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\"")
            .isZero
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFloatFieldIsZero(
        fieldName: String,
        getterName: String
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Float>(fieldName, getterName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
            .isZero
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFloatField(fieldName: String, expected: Float, accuracy: Float): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Float>(fieldName)
        if (value == null) {
            softAssertions.fail("Expected field \"$fieldName\" to be nonnull")
        } else {
            softAssertions.assertThat(abs(value - expected))
                .`as`("Field \"$fieldName\"")
                .isLessThanOrEqualTo(accuracy)
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFloatField(
        fieldName: String,
        getterName: String,
        expected: Float,
        accuracy: Float
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Float>(fieldName, getterName)
        if (value == null) {
            softAssertions.fail("Expected field \"$fieldName\" to be nonnull")
        } else {
            softAssertions.assertThat(abs(value - expected))
                .`as`("Field \"$fieldName\" from getter \"$getterName\"")
                .isLessThanOrEqualTo(accuracy)
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkDecimalField(
        fieldName: String,
        getterName: String,
        expected: BigDecimal?,
        percentage: Double
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<BigDecimal>(fieldName, getterName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
            .isCloseTo(expected, Assertions.withinPercentage(percentage))
        unverifiedFields.remove(fieldName)
        return this
    }
    // endregion

    // region checkField iterable
    override fun <P> checkField(
        fieldName: String,
        expected: Iterable<P>?,
        sameOrder: Boolean
    ): ObjectPropertyAssertions<T> {
        val collection = getValueOfField<Iterable<P>>(fieldName)
        val assertValue = softAssertions.assertThat(collection)
            .`as`("Field \"$fieldName\"")
        if (expected == null) {
            assertValue.isNull()
        } else {
            val assertThatIterable = assertValue.usingRecursiveFieldByFieldElementComparator()
            if (sameOrder) {
                assertThatIterable.containsExactlyElementsOf(expected)
            } else {
                assertThatIterable.containsExactlyInAnyOrderElementsOf(expected)
            }
        }
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun <P> checkField(fieldName: String, expected: Array<P>?): ObjectPropertyAssertions<T> {
        return checkField(fieldName, expected, true)
    }

    override fun <P> checkField(
        fieldName: String,
        expected: Array<P>?,
        sameOrder: Boolean
    ): ObjectPropertyAssertions<T> {
        val array = getValueOfField<Array<P>>(fieldName)
        val assertValue = softAssertions.assertThat(array)
            .`as`("Field \"$fieldName\"")
        if (expected == null) {
            assertValue.isNull()
        } else {
            val assertThatArray = assertValue.usingFieldByFieldElementComparator()
            if (sameOrder) {
                assertThatArray.containsExactly(*expected)
            } else {
                assertThatArray.containsExactlyInAnyOrder(*expected)
            }
        }
        unverifiedFields.remove(fieldName)
        return this
    }
    // endregion

    // region checkFieldIsInstanceOf
    override fun checkFieldIsInstanceOf(
        fieldName: String,
        type: Class<*>
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\"")
            .isInstanceOf(type)
        unverifiedFields.remove(fieldName)
        return this
    }

    override fun checkFieldIsInstanceOf(
        fieldName: String,
        getterName: String,
        type: Class<*>
    ): ObjectPropertyAssertions<T> {
        val value = getValueOfField<Any>(fieldName, getterName)
        softAssertions.assertThat(value)
            .`as`("Field \"$fieldName\" from getter \"$getterName\"")
            .isInstanceOf(type)
        unverifiedFields.remove(fieldName)
        return this
    }
    // endregion

    // region settings
    override fun withIgnoredFields(vararg ignoredFields: String): ObjectPropertyAssertions<T> {
        this.ignoredFields.clear()
        this.ignoredFields.addAll(ignoredFields)
        reInitializeFields()
        return this
    }

    override fun withPermittedFields(vararg permittedFields: String): ObjectPropertyAssertions<T> {
        this.permittedFields.clear()
        this.permittedFields.addAll(permittedFields)
        reInitializeFields()
        return this
    }

    override fun withFinalFieldOnly(value: Boolean): ObjectPropertyAssertions<T> {
        finalFieldsOnly = value
        reInitializeFields()
        return this
    }

    override fun withDeclaredAccessibleFields(value: Boolean): ObjectPropertyAssertions<T> {
        declaredAccessibleFields = value
        reInitializeFields()
        return this
    }

    override fun withPrivateFields(value: Boolean): ObjectPropertyAssertions<T> {
        privateFields = value
        reInitializeFields()
        return this
    }

    override fun includingParents(value: Boolean): ObjectPropertyAssertions<T> {
        includingParents = value
        reInitializeFields()
        return this
    }
    // endregion

    // region common
    protected open fun reInitializeFields() {
        allFields = HashMap()
        unverifiedFields = HashMap()
        val matcher = createFieldMatcher()
        val foundFields = getFieldsFromClass(
            actual, includingParents, declaredAccessibleFields, privateFields
        )
        for (field in foundFields) {
            allFields[field.name] = field
            if (matcher.match(field).matches) {
                unverifiedFields[field.name] = field
            }
        }
    }

    protected fun createFieldMatcher(): FieldMatcher {
        return CompositeFieldMatcher(
            listOf(
                NonStaticModifierFieldMatcher(),
                FinalModifierFieldMatcher(finalFieldsOnly),
                PermittedFieldsMatcher(permittedFields),
                IgnoredFieldsMatcher(ignoredFields)
            )
        )
    }

    override fun checkAll() {
        softAssertions.assertThat(unverifiedFields).`as`("Remaining unverified fields").isEmpty()
        softAssertions.assertAll()
    }

    override fun getUnverifiedFieldsCount() = unverifiedFields.size

    protected open fun <S : Any> createNestedAssertions(actual: S?): ObjectPropertyAssertions<S> {
        return ObjectPropertyAssertions(actual)
    }

    protected abstract fun <F> getValueOfField(fieldName: String): F?

    protected fun <F> getValueOfField(fieldName: String, getterName: String): F? {
        try {
            val field = allFields[fieldName]
            if (field == null) {
                softAssertions.fail("Actual object does not contain field \"$fieldName\"")
                return null
            }
            if (doesFieldMatch(field)) {
                val method: Method = actual::class.java.getMethod(getterName)
                return method.invoke(actual) as F
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalAccessException,
                is InvocationTargetException,
                is NoSuchMethodException ->
                    softAssertions.fail("Field \"$fieldName\" with getter \"$getterName\" is not accessible with cause $e")
                else -> throw e
            }
        }
        return null
    }

    protected fun doesFieldMatch(field: ClassField): Boolean {
        val (matches, noMatchCause) = createFieldMatcher().match(field)
        if (matches) {
            return true
        } else {
            softAssertions.fail("Field \"${field.name}\" does not match precondition. $noMatchCause. Actual object: $actual")
        }
        return false
    }
    // endregion
}
