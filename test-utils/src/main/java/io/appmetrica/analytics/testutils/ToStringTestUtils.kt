package io.appmetrica.analytics.testutils

import androidx.annotation.VisibleForTesting
import org.assertj.core.api.SoftAssertions
import java.lang.reflect.Modifier
import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

object ToStringTestUtils {

    private val valueQuotes = listOf("", "'", "\"")
    private val DEFAULT_EXCLUDED_FIELDS = setOf("tag")

    enum class ArrayFormat {
        /** Full content: [1, 2, 3] */
        FULL,

        /** Short format: array[3] */
        SHORT,

        /** Native hashCode: [I@1b6d3586 */
        NATIVE
    }

    @JvmStatic
    @JvmOverloads
    fun extractFieldsAndValues(
        clazz: Any,
        instance: Any,
        modifierPreconditions: Int = 0,
        excludedFields: Set<String> = emptySet(),
        supportedArrayFormats: Set<ArrayFormat> = setOf(ArrayFormat.FULL, ArrayFormat.SHORT)
    ): List<Predicate<String>> {
        return when {
            clazz is KClass<*> -> extractFromKotlinClass(
                clazz, instance, modifierPreconditions, excludedFields, supportedArrayFormats
            )

            clazz is Class<*> && isKotlinClass(clazz) -> extractFromKotlinClass(
                clazz.kotlin, instance, modifierPreconditions, excludedFields, supportedArrayFormats
            )

            clazz is Class<*> -> extractFromJavaClass(
                clazz, instance, modifierPreconditions, excludedFields, supportedArrayFormats
            )

            clazz is String -> extractFieldsAndValues(
                Class.forName(clazz), instance, modifierPreconditions, excludedFields, supportedArrayFormats
            )

            else -> throw IllegalArgumentException(
                "Clazz must be Class, KClass, or String, but was ${clazz.javaClass}"
            )
        }
    }

    @JvmStatic
    @VisibleForTesting
    fun isKotlinClass(clazz: Class<*>): Boolean {
        return clazz.annotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
    }

    private fun extractFromKotlinClass(
        kClass: KClass<*>,
        instance: Any,
        modifierPreconditions: Int,
        excludedFields: Set<String>,
        supportedArrayFormats: Set<ArrayFormat>
    ): List<Predicate<String>> {
        val result = mutableListOf<Predicate<String>>()
        val allExcludedFields = DEFAULT_EXCLUDED_FIELDS + excludedFields

        for (property in kClass.declaredMemberProperties) {
            val fieldName = property.name
            if (allExcludedFields.contains(fieldName)) {
                continue
            }

            val javaField = property.javaField ?: continue

            if (!modifiersMatchPreconditions(javaField.modifiers, modifierPreconditions)) {
                continue
            }

            property.isAccessible = true
            val value = property.getter.call(instance)

            result.add(createArrayAwarePredicate(fieldName, value, supportedArrayFormats))
        }

        return result
    }

    private fun extractFromJavaClass(
        clazz: Class<*>,
        instance: Any,
        modifierPreconditions: Int,
        excludedFields: Set<String>,
        supportedArrayFormats: Set<ArrayFormat>
    ): List<Predicate<String>> {
        val result = mutableListOf<Predicate<String>>()

        for (field in clazz.declaredFields) {
            if (excludedFields.contains(field.name)) {
                continue
            }

            if (!modifiersMatchPreconditions(field.modifiers, modifierPreconditions)) {
                continue
            }

            field.isAccessible = true
            val value = field.get(instance)

            result.add(createArrayAwarePredicate(field.name, value, supportedArrayFormats))
        }

        return result
    }

    private fun createArrayAwarePredicate(
        fieldName: String,
        value: Any?,
        supportedFormats: Set<ArrayFormat>
    ): Predicate<String> {
        val fullFormatValue = extractStringFromValue(value)
        val arraySize = getArraySize(value)
        val nativeFormat = if (arraySize != null) value.toString() else null

        return object : Predicate<String> {
            override fun test(s: String): Boolean {
                for (valueQuote in valueQuotes) {
                    // Try full format if supported
                    if (supportedFormats.contains(ArrayFormat.FULL)) {
                        val fullPair = "$fieldName=$valueQuote$fullFormatValue$valueQuote"
                        if (s.contains(fullPair)) {
                            return true
                        }
                    }

                    // For arrays, try additional formats
                    if (arraySize != null) {
                        // Try short format if supported
                        if (supportedFormats.contains(ArrayFormat.SHORT)) {
                            val shortFormat = "$fieldName=${valueQuote}array[$arraySize]$valueQuote"
                            if (s.contains(shortFormat)) {
                                return true
                            }
                        }

                        // Try native format if supported
                        if (supportedFormats.contains(ArrayFormat.NATIVE) && nativeFormat != null) {
                            val nativePair = "$fieldName=$valueQuote$nativeFormat$valueQuote"
                            if (s.contains(nativePair)) {
                                return true
                            }
                        }
                    }
                }
                return false
            }

            override fun toString(): String {
                val formats = mutableListOf<String>()

                if (supportedFormats.contains(ArrayFormat.FULL)) {
                    formats.add("$fieldName=$fullFormatValue")
                }

                if (arraySize != null) {
                    if (supportedFormats.contains(ArrayFormat.SHORT)) {
                        formats.add("$fieldName=array[$arraySize]")
                    }
                    if (supportedFormats.contains(ArrayFormat.NATIVE) && nativeFormat != null) {
                        formats.add("$fieldName=$nativeFormat")
                    }
                }

                return if (formats.size > 1) {
                    "contains pair: ${formats.joinToString(" or ")}"
                } else {
                    "contains pair: ${formats.firstOrNull() ?: "$fieldName=$fullFormatValue"}"
                }
            }
        }
    }

    /**
     * Extracts string representation from a value using Kotlin-style contentToString().
     */
    @JvmStatic
    @VisibleForTesting
    fun extractStringFromValue(value: Any?): String {
        return when (value) {
            null -> "null"
            // Kotlin primitive arrays
            is IntArray -> value.contentToString()
            is LongArray -> value.contentToString()
            is ShortArray -> value.contentToString()
            is ByteArray -> value.contentToString()
            is FloatArray -> value.contentToString()
            is DoubleArray -> value.contentToString()
            is BooleanArray -> value.contentToString()
            is CharArray -> value.contentToString()
            // Kotlin object arrays
            is Array<*> -> value.contentToString()
            // Regular objects
            else -> value.toString()
        }
    }

    @JvmStatic
    @VisibleForTesting
    fun getArraySize(value: Any?): Int? {
        return when (value) {
            null -> null
            is IntArray -> value.size
            is LongArray -> value.size
            is ShortArray -> value.size
            is ByteArray -> value.size
            is FloatArray -> value.size
            is DoubleArray -> value.size
            is BooleanArray -> value.size
            is CharArray -> value.size
            is Array<*> -> value.size
            else -> null
        }
    }

    @JvmStatic
    fun checkToString(actualValue: Any, extractedFieldAndValuesPredicates: List<Predicate<String>>) {
        val toStringValue = actualValue.toString()

        val softly = SoftAssertions()
        for (fieldAndValuePredicate in extractedFieldAndValuesPredicates) {
            softly.assertThat(toStringValue).matches(
                fieldAndValuePredicate,
                "Containing pair: $fieldAndValuePredicate)"
            )
        }
        softly.assertAll()
    }

    @JvmStatic
    @VisibleForTesting
    fun modifiersMatchPreconditions(modifier: Int, modifierPreconditions: Int): Boolean {
        if (Modifier.isStatic(modifier)) {
            return false
        }

        for (i in 0 until 32) {
            val currentBit = 1 shl i
            if ((currentBit and modifierPreconditions) != 0 && (modifier and currentBit) == 0) {
                return false
            }
        }

        return true
    }
}
