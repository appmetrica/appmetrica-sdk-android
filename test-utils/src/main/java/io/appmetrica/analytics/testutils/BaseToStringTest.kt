package io.appmetrica.analytics.testutils

import org.junit.Test

abstract class BaseToStringTest(
    actualValue: Any?,
    private val modifierPreconditions: Int,
    private val excludedFields: Set<String>?,
    @Suppress("UNUSED_PARAMETER") additionalDescription: String?,
    private val supportedArrayFormats: Set<ToStringTestUtils.ArrayFormat> =
        setOf(ToStringTestUtils.ArrayFormat.FULL, ToStringTestUtils.ArrayFormat.SHORT)
) : CommonTest() {

    private val actualValue: Any = actualValue
        ?: throw IllegalArgumentException("actualValue cannot be null")

    private val clazz: Any = this.actualValue::class

    constructor(
        actualValue: Any?,
        modifierPreconditions: Int,
        additionalDescription: String?
    ) : this(actualValue, modifierPreconditions, emptySet(), additionalDescription)

    constructor(
        actualValue: Any?,
        modifierPreconditions: Int,
        excludedFields: Set<String>?,
        additionalDescription: String?
    ) : this(
        actualValue,
        modifierPreconditions,
        excludedFields,
        additionalDescription,
        setOf(ToStringTestUtils.ArrayFormat.FULL, ToStringTestUtils.ArrayFormat.SHORT)
    )

    @Test
    fun toStringContainsAllFields() {
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            clazz,
            actualValue,
            modifierPreconditions,
            excludedFields ?: emptySet(),
            supportedArrayFormats
        )
        ToStringTestUtils.checkToString(actualValue, extractedFieldAndValues)
    }

    companion object {
        @JvmStatic
        fun Any.toTestCase(
            modifierPreconditions: Int = 0,
            excludedFields: Set<String> = emptySet(),
            additionalDescription: String = ""
        ): Array<Any?> = arrayOf(
            this,
            modifierPreconditions,
            excludedFields,
            additionalDescription
        )
    }
}
