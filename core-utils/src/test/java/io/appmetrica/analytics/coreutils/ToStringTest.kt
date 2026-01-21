package io.appmetrica.analytics.coreutils

import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ToStringTestUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ToStringTest(
    clazz: Any?,
    actualValue: Any,
    modifierPreconditions: Int,
    additionalDescription: String?
) : CommonTest() {

    private var clazz: Class<*>? = null
    private val actualValue: Any
    private val modifierPreconditions: Int

    init {
        if (clazz is Class<*>) {
            this.clazz = clazz
        } else if (clazz is String) {
            this.clazz = Class.forName(clazz as String?)
        } else {
            throw IllegalArgumentException("Clazz must be instance of Class or String")
        }
        this.actualValue = actualValue
        this.modifierPreconditions = modifierPreconditions
    }

    @Test
    fun toStringContainsAllFields() {
        val excludedFields = setOf("timeProvider")
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            clazz,
            actualValue,
            modifierPreconditions,
            excludedFields
        )
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues)
    }

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
        @JvmStatic
        fun data(): List<Array<Any>> {
            return listOf(
                arrayOf(
                    CachedDataProvider.CachedData::class.java,
                    CachedDataProvider.CachedData<Any>(10L, 20L, "some description"),
                    0,
                    ""
                )
            )
        }
    }
}
