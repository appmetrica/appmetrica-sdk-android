package io.appmetrica.analytics.impl.clids

import android.content.ContentValues
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyMap
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

private const val KEY_CLIDS = "clids"

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class ClidsDataParserTest(
    private val values: ContentValues,
    private val inputMap: Map<String, String>?,
    private val validClids: Boolean,
    private val expected: Map<String, String>?
) : CommonTest() {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any?>> {
            return listOf(
                // #0
                arrayOf(ContentValues(), null, true, null),
                arrayOf(ContentValues(), null, false, null),
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, "") }, null, true, null),
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, "") }, null, false, null),
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, 4) }, null, true, null),

                // #5
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, 4) }, null, false, null),
                arrayOf(
                    ContentValues().also { it.put(KEY_CLIDS, "{}") },
                    emptyMap<String, String>(),
                    true,
                    emptyMap<String, String>()
                ),
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, "{}") }, emptyMap<String, String>(), false, null),
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, "bad_json") }, null, true, null),
                arrayOf(ContentValues().also { it.put(KEY_CLIDS, "bad_json") }, null, false, null),

                // #10
                arrayOf(
                    ContentValues().also { it.put(KEY_CLIDS, JSONObject().put("clid0", "0").toString()) },
                    mapOf("clid0" to "0"),
                    false,
                    null
                ),
                arrayOf(
                    ContentValues().also { it.put(KEY_CLIDS, JSONObject().put("clid0", "0").toString()) },
                    mapOf("clid0" to "0"),
                    true,
                    mapOf("clid0" to "0")
                ),
                arrayOf(
                    ContentValues().also { it.put(KEY_CLIDS, JSONObject().put("clid0", 0).toString()) },
                    mapOf("clid0" to "0"),
                    true,
                    mapOf("clid0" to "0")
                ),
                arrayOf(
                    ContentValues().also {
                        it.put(
                            KEY_CLIDS,
                            JSONObject().put("clid0", "0").put("clid1", "1").toString()
                        )
                    },
                    mapOf("clid0" to "0", "clid1" to "1"),
                    true,
                    mapOf("clid0" to "0", "clid1" to "1")
                ),
                arrayOf(
                    ContentValues().also {
                        it.put(KEY_CLIDS, JSONObject().put("clid0", "0").toString())
                        it.put("another_key", JSONObject().put("clid1", "1").toString())
                    },
                    mapOf("clid0" to "0"),
                    true,
                    mapOf("clid0" to "0")
                ),

                // #15
                arrayOf(
                    ContentValues().also {
                        it.put("another_key", JSONObject().put("clid1", "1").toString())
                    },
                    null,
                    true,
                    null
                ),
            )
        }
    }

    @get:Rule
    val startupUtils = MockedStaticRule(StartupUtils::class.java)
    private val dataParser = ClidsDataParser()

    @Test
    fun parseClids() {
        whenever(StartupUtils.isValidClids(anyMap())).thenReturn(validClids)
        assertThat(dataParser.invoke(values)).isEqualTo(expected)
        startupUtils.staticMock.verify { StartupUtils.isValidClids(inputMap) }
    }
}
