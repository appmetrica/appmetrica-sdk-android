package io.appmetrica.analytics.impl

import io.appmetrica.analytics.ModuleEvent.Category
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class EventCategoryToSourceConverterTest(
    private val input: Category,
    private val expected: EventSource
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(Category.GENERAL, EventSource.NATIVE),
            arrayOf(Category.SYSTEM, EventSource.SYSTEM)
        )
    }

    private val converter by setUp { EventCategoryToSourceConverter() }

    @Test
    fun convert() {
        assertThat(converter.convert(input)).isEqualTo(expected)
    }
}
