package io.appmetrica.analytics.impl.utils.limitation.hierarchical

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
internal class HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmerTest(
    private val inputMap: Map<String, String>?,
    private val expectedMap: Map<String, String>?,
    private val pairsTruncated: Int,
    private val bytesTruncated: Int,
    @Suppress("UNUSED_PARAMETER") description: String
) : CommonTest() {

    private val keyTrimmer: HierarchicalStringTrimmer = mock {
        on { trim(anyOrNull()) } doAnswer { invocation ->
            val input = invocation.getArgument<String?>(0)
            trimToLimit(input, MAP_KEY_LIMIT)
        }
    }

    private val valueTrimmer: HierarchicalStringTrimmer = mock {
        on { trim(anyOrNull()) } doAnswer { invocation ->
            val input = invocation.getArgument<String?>(0)
            trimToLimit(input, MAP_VALUE_LIMIT)
        }
    }

    private val mapTrimmer by setUp {
        HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(
            MAP_SIZE_LIMIT,
            keyTrimmer,
            valueTrimmer
        )
    }

    private fun trimToLimit(input: String?, limit: Int): TrimmingResult<String, BytesTruncatedProvider> {
        var result = input
        var truncated = 0
        if (input != null && input.length > limit) {
            result = input.substring(0, limit)
            truncated = input.length - result.length
        }
        return TrimmingResult(result, BytesTruncatedInfo(truncated))
    }

    @Test
    fun trim() {
        val trimmingResult = mapTrimmer.trim(inputMap)

        ObjectPropertyAssertions(trimmingResult).apply {
            checkField("value", expectedMap)
            checkFieldComparingFieldByField(
                "metaInfo",
                CollectionTrimInfo(pairsTruncated, bytesTruncated)
            )
            checkAll()
        }
    }

    companion object {
        private const val KEY_WITH_5_BYTES_1 = "Key#1"
        private const val KEY_WITH_5_BYTES_2 = "Key#2"
        private const val KEY_WITH_5_BYTES_3 = "Key#3"
        private const val KEY_WITH_5_BYTES_4 = "Key#4"
        private const val KEY_WITH_5_BYTES_5 = "Key#5"
        private const val KEY_WITH_20_BYTES_1 = "Key#21_ _ _ _ _ _ _ "
        private const val KEY_WITH_20_BYTES_2 = "Key#22_ _ _ _ _ _ _ "
        private const val KEY_WITH_20_BYTES_3 = "Key#23_ _ _ _ _ _ _ "
        private const val TRUNCATED_KEY_WITH_20_BYTES_1 = "Key#21_ _ "
        private const val KEY_WITH_1_BYTE = "k"
        private const val KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS = "Key_-ААААА"
        private const val KEY_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS = "Key_-ААААА_ _ _ _ _ "
        private const val KEY_WITH_COLLISION_1 = "Collision#1"
        private const val KEY_WITH_COLLISION_2 = "Collision#2"
        private const val KEY_WITH_COLLISION_3 = "Collision#3"
        private const val TRUNCATED_KEY_WITH_COLLISION = "Collision#"

        private const val VALUE_WITH_10_BYTES = "Value#1_ _"
        private const val VALUE_WITH_11_BYTES = "Value#2_ _ "
        private const val VALUE_WITH_12_BYTES = "Value#3_ _ _"
        private const val VALUE_WITH_13_BYTES = "Value#4_ _ _ "
        private const val VALUE_WITH_14_BYTES = "Value#5_ _ _ _"
        private const val VALUE_WITH_30_BYTES = "Value#21_ _ _ _ _ _ _ _ _ _ _ "
        private const val VALUE_WITH_31_BYTES = "Value#22_ _ _ _ _ _ _ _ _ _ _ _"
        private const val VALUE_WITH_32_BYTES = "Value#23_ _ _ _ _ _ _ _ _ _ _ _ "
        private const val TRUNCATED_VALUE_WITH_30_BYTES = "Value#21_ _ _ _ _ _ "
        private const val VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS = "ValueWithCyril_ААААА"
        private const val VALUE_WITH_35_BYTES_INCLUDING_CYRILLIC_CHARS = "ValueWithCyril_АААААБББББ"
        private const val VALUE_WITH_SINGLE_BYTE = "v"

        private const val MAP_SIZE_LIMIT = 50
        private const val MAP_KEY_LIMIT = 10
        private const val MAP_VALUE_LIMIT = 20

        @JvmStatic
        @Parameterized.Parameters(name = "#{index} - {4}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(null, null, 0, 0, "null"),
            arrayOf(emptyMap<String, String>(), emptyMap<String, String>(), 0, 0, "empty map"),
            arrayOf(
                mapOf(KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES),
                mapOf(KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES),
                0,
                0,
                "map with single short pair"
            ),
            arrayOf(
                mapOf(KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS to VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS),
                mapOf(KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS to VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS),
                0,
                0,
                "map with single short pair with cyrillic chars"
            ),
            arrayOf(
                mapOf(KEY_WITH_20_BYTES_1 to VALUE_WITH_30_BYTES),
                mapOf(TRUNCATED_KEY_WITH_20_BYTES_1 to TRUNCATED_VALUE_WITH_30_BYTES),
                0,
                20,
                "map with single large pair"
            ),
            arrayOf(
                mapOf(KEY_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS to VALUE_WITH_35_BYTES_INCLUDING_CYRILLIC_CHARS),
                mapOf(KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS to VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS),
                0,
                15,
                "map with single large pair with cyrillic chars"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES,
                    KEY_WITH_5_BYTES_4 to VALUE_WITH_13_BYTES,
                    KEY_WITH_5_BYTES_5 to VALUE_WITH_14_BYTES
                ),
                mapOf(
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES
                ),
                2,
                37,
                "map with 2 small items out of map size limit"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_5_BYTES_4 to VALUE_WITH_13_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES,
                    KEY_WITH_5_BYTES_5 to VALUE_WITH_14_BYTES,
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES
                ),
                mapOf(
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES
                ),
                2,
                37,
                "map with 2 small items out of map size limit in non sorted order"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_5_BYTES_4 to VALUE_WITH_13_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES,
                    KEY_WITH_5_BYTES_5 to VALUE_WITH_14_BYTES,
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_1_BYTE to null
                ),
                mapOf(
                    KEY_WITH_1_BYTE to null,
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES
                ),
                2,
                37,
                "map with null-value and some items out of map size limit"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_5_BYTES_4 to VALUE_WITH_13_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES,
                    KEY_WITH_5_BYTES_5 to VALUE_WITH_14_BYTES,
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_1_BYTE to ""
                ),
                mapOf(
                    KEY_WITH_1_BYTE to "",
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_5_BYTES_2 to VALUE_WITH_11_BYTES,
                    KEY_WITH_5_BYTES_3 to VALUE_WITH_12_BYTES
                ),
                2,
                37,
                "map with single empty value and some items out of map limit"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_20_BYTES_1 to VALUE_WITH_30_BYTES,
                    KEY_WITH_20_BYTES_2 to VALUE_WITH_31_BYTES,
                    KEY_WITH_20_BYTES_3 to VALUE_WITH_32_BYTES,
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_1_BYTE to ""
                ),
                mapOf(
                    KEY_WITH_1_BYTE to "",
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    TRUNCATED_KEY_WITH_20_BYTES_1 to TRUNCATED_VALUE_WITH_30_BYTES
                ),
                2,
                123,
                "map with some pairs out of limit"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_20_BYTES_1 to VALUE_WITH_30_BYTES,
                    KEY_WITH_20_BYTES_2 to VALUE_WITH_31_BYTES,
                    KEY_WITH_20_BYTES_3 to VALUE_WITH_32_BYTES,
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES,
                    KEY_WITH_1_BYTE to "",
                    KEY_WITH_15_BYTES_INCLUDING_CYRILLIC_CHARS to VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS,
                    KEY_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS to VALUE_WITH_25_BYTES_INCLUDING_CYRILLIC_CHARS
                ),
                mapOf(
                    KEY_WITH_1_BYTE to "",
                    KEY_WITH_5_BYTES_1 to VALUE_WITH_10_BYTES
                ),
                5,
                243,
                "map with some pairs out of limit including cyrillic"
            ),
            arrayOf(
                mapOf(
                    KEY_WITH_COLLISION_1 to VALUE_WITH_SINGLE_BYTE,
                    KEY_WITH_COLLISION_2 to VALUE_WITH_SINGLE_BYTE,
                    KEY_WITH_COLLISION_3 to VALUE_WITH_SINGLE_BYTE
                ),
                mapOf(
                    TRUNCATED_KEY_WITH_COLLISION to VALUE_WITH_SINGLE_BYTE
                ),
                0,
                3,
                "map with truncated key collision"
            )
        )
    }
}
