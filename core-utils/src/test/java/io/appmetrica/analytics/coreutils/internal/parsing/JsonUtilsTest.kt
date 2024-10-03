package io.appmetrica.analytics.coreutils.internal.parsing

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.isEqualTo
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrDefault
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNullable
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optFloatOrDefault
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optFloatOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optHexByteArray
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optJsonObjectOrDefault
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optJsonObjectOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrDefault
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optStringOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optStringOrNullable
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class JsonUtilsTest {

    private val valueKey = "Some value key"

    @Test
    fun `optLongOrNull for null`() {
        assertThat(null.optLongOrNull(valueKey)).isNull()
    }

    @Test
    fun `optLongOrNull for empty json`() {
        assertThat(JSONObject().optLongOrNull(valueKey)).isNull()
    }

    @Test
    fun `optLongOrNull for wrong value`() {
        val input = JSONObject().put(valueKey, "Wrong value")
        assertThat(input.optLongOrNull(valueKey)).isNull()
    }

    @Test
    fun `optLongOrNull for value`() {
        val value = 23423L
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optLongOrNull(valueKey)).isEqualTo(value)
    }

    @Test
    fun `optLongOrDefault for null`() {
        assertThat(null.optLongOrDefault(valueKey, 0L)).isEqualTo(0L)
    }

    @Test
    fun `optLongOrDefault for empty json`() {
        assertThat(JSONObject().optLongOrDefault(valueKey, 0L)).isEqualTo(0L)
    }

    @Test
    fun `optLongOrDefault for wrong value`() {
        val input = JSONObject().put(valueKey, "Wrong value")
        assertThat(input.optLongOrDefault(valueKey, 0L)).isEqualTo(0L)
    }

    @Test
    fun `optLongOrDefault for value`() {
        val value = 213234231L
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optLongOrDefault(valueKey, 0L)).isEqualTo(value)
    }

    @Test
    fun `optFloatOrDefault for null`() {
        assertThat(null.optFloatOrDefault(valueKey, 0f)).isEqualTo(0f)
    }

    @Test
    fun `optFloatOrDefault for empty json`() {
        assertThat(JSONObject().optFloatOrDefault(valueKey, 0f)).isEqualTo(0f)
    }

    @Test
    fun `optFloatOrDefault for wrong value`() {
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optFloatOrDefault(valueKey, 0f)).isEqualTo(0f)
    }

    @Test
    fun `optFloatOrDefault for value`() {
        val value = 234.3432f
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optFloatOrDefault(valueKey, 0f)).isEqualTo(value)
    }

    @Test
    fun `optFloatOrNull for null`() {
        assertThat(null.optFloatOrNull(valueKey)).isNull()
    }

    @Test
    fun `optFloatOrNull for empty json`() {
        assertThat(JSONObject().optFloatOrNull(valueKey)).isNull()
    }

    @Test
    fun `optFloatOrNull for wrong value`() {
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optFloatOrNull(valueKey)).isNull()
    }

    @Test
    fun `optFloatOrNull for valid value`() {
        val value = 3242f
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optFloatOrNull(valueKey)).isEqualTo(value)
    }

    @Test
    fun `optStringOrDefault for null`() {
        val default = "default value"
        assertThat(null.optStringOrNullable(valueKey, default)).isEqualTo(default)
    }

    @Test
    fun `optStringOrDefault for empty json`() {
        val default = "default value"
        assertThat(JSONObject().optStringOrNullable(valueKey, default)).isEqualTo(default)
    }

    @Test
    fun `optStringOrDefault for valid value`() {
        val value = "some valid value"
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optStringOrNullable(valueKey, "default value")).isEqualTo(value)
    }

    @Test
    fun `optStringOrNull for null`() {
        assertThat(null.optStringOrNull(valueKey)).isNull()
    }

    @Test
    fun `optStringOrNull for empty json`() {
        assertThat(JSONObject().optStringOrNull(valueKey)).isNull()
    }

    @Test
    fun `optStringOrNull for valid value`() {
        val value = "Some value"
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optStringOrNull(valueKey)).isEqualTo(value)
    }

    @Test
    fun `optBooleanOrDefaultNullable for null`() {
        assertThat(null.optBooleanOrNullable(valueKey, false)).isFalse()
    }

    @Test
    fun `optBooleanOrDefaultNullable for empty json`() {
        assertThat(JSONObject().optBooleanOrNullable(valueKey, false)).isFalse()
    }

    @Test
    fun `optBooleanOrDefaultNullable for wrong value`() {
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optBooleanOrNullable(valueKey, false)).isFalse()
    }

    @Test
    fun `optBooleanOrDefaultNullable for valid value`() {
        val input = JSONObject().put(valueKey, true)
        assertThat(input.optBooleanOrNullable(valueKey, false)).isTrue()
    }

    @Test
    fun `optBooleanOrNull for null`() {
        assertThat(null.optBooleanOrNull(valueKey)).isNull()
    }

    @Test
    fun `optBooleanOrNull for empty json`() {
        assertThat(JSONObject().optBooleanOrNull(valueKey)).isNull()
    }

    @Test
    fun `optBooleanOrNull for wrong value`() {
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optBooleanOrNull(valueKey)).isNull()
    }

    @Test
    fun `optBooleanOrNull for valid value`() {
        val input = JSONObject().put(valueKey, true)
        assertThat(input.optBooleanOrNull(valueKey)).isTrue()
    }

    @Test
    fun `optBooleanOrDefault for null`() {
        assertThat(null.optBooleanOrDefault(valueKey, false)).isFalse()
    }

    @Test
    fun `optBooleanOrDefault for empty json`() {
        assertThat(JSONObject().optBooleanOrDefault(valueKey, false)).isFalse()
    }

    @Test
    fun `optBooleanOrDefault for wrong value`() {
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optBooleanOrDefault(valueKey, false)).isFalse()
    }

    @Test
    fun `optBooleanOrDefault for valid value`() {
        val input = JSONObject().put(valueKey, true)
        assertThat(input.optBooleanOrDefault(valueKey, false)).isTrue()
    }

    @Test
    fun `optJsonObjectOrDefault for null`() {
        val default = JSONObject()
        assertThat(null.optJsonObjectOrDefault(valueKey, default)).isEqualTo(default)
    }

    @Test
    fun `optJsonObjectOrDefault for empty json`() {
        val default = JSONObject()
        assertThat(JSONObject().optJsonObjectOrDefault(valueKey, default)).isEqualTo(default)
    }

    @Test
    fun `optJsonObjectOrDefault for wrong value`() {
        val default = JSONObject()
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optJsonObjectOrDefault(valueKey, default)).isEqualTo(default)
    }

    @Test
    fun `optJsonObjectOrDefault for valid value`() {
        val validValue = JSONObject()
        val input = JSONObject().put(valueKey, validValue)
        assertThat(input.optJsonObjectOrDefault(valueKey, JSONObject())).isEqualTo(validValue)
    }

    @Test
    fun `optJsonObjectOrNull for null`() {
        assertThat(null.optJsonObjectOrNull(valueKey)).isNull()
    }

    @Test
    fun `optJsonObjectOrNull for empty json`() {
        assertThat(JSONObject().optJsonObjectOrNull(valueKey)).isNull()
    }

    @Test
    fun `optJsonObjectOrNull for wrong value`() {
        val input = JSONObject().put(valueKey, "wrong value")
        assertThat(input.optJsonObjectOrNull(valueKey)).isNull()
    }

    @Test
    fun `optJsonObjectOrNull for valid value`() {
        val value = JSONObject()
        val input = JSONObject().put(valueKey, value)
        assertThat(input.optJsonObjectOrNull(valueKey)).isEqualTo(value)
    }

    @Test
    fun `isEqualTo if equal`() {
        val firstJSONObject = JSONObject().apply {
            put("jsonArray", JSONArray().apply {
                put(1)
                put("string")
                put(JSONObject().apply {
                    put("jsonArray-jsonObject-key-1", "jsonArray-jsonObject-value-1")
                    put("jsonArray-jsonObject-key-2", "jsonArray-jsonObject-value-2")
                })
            })
            put("jsonObject-key-1", "jsonObject-value-1")
            put("jsonObject-key-2", 42)
            put("jsonObject-key-3", JSONObject().apply {
                put("jsonObject-jsonObject-key-1", "jsonObject-jsonObject-value-1")
                put("jsonObject-jsonObject-key-2", "jsonObject-jsonObject-value-2")
            })
        }
        val secondJSONObject = JSONObject().apply {
            put("jsonObject-key-2", 42)
            put("jsonObject-key-1", "jsonObject-value-1")
            put("jsonArray", JSONArray().apply {
                put(1)
                put("string")
                put(JSONObject().apply {
                    put("jsonArray-jsonObject-key-1", "jsonArray-jsonObject-value-1")
                    put("jsonArray-jsonObject-key-2", "jsonArray-jsonObject-value-2")
                })
            })
            put("jsonObject-key-3", JSONObject().apply {
                put("jsonObject-jsonObject-key-2", "jsonObject-jsonObject-value-2")
                put("jsonObject-jsonObject-key-1", "jsonObject-jsonObject-value-1")
            })
        }

        assertThat(firstJSONObject)
            .usingComparator { p0, p1 ->
                if (p0!!.isEqualTo(p1!!)) 0 else 1
            }
            .isEqualTo(secondJSONObject)
    }

    @Test
    fun `isEqualTo if not equal since jsonArray items order differ`() {
        val firstJSONObject = JSONObject().apply {
            put("jsonArray", JSONArray().apply {
                put(1)
                put("string")
                put(JSONObject().apply {
                    put("jsonArray-jsonObject-key-1", "jsonArray-jsonObject-value-1")
                    put("jsonArray-jsonObject-key-2", "jsonArray-jsonObject-value-2")
                })
            })
            put("jsonObject-key-1", "jsonObject-value-1")
            put("jsonObject-key-2", 42)
            put("jsonObject-key-3", JSONObject().apply {
                put("jsonObject-jsonObject-key-1", "jsonObject-jsonObject-value-1")
                put("jsonObject-jsonObject-key-2", "jsonObject-jsonObject-value-2")
            })
        }
        val secondJSONObject = JSONObject().apply {
            put("jsonArray", JSONArray().apply {
                put("string")
                put(1)
                put(JSONObject().apply {
                    put("jsonArray-jsonObject-key-1", "jsonArray-jsonObject-value-1")
                    put("jsonArray-jsonObject-key-2", "jsonArray-jsonObject-value-2")
                })
            })
            put("jsonObject-key-1", "jsonObject-value-1")
            put("jsonObject-key-2", 42)
            put("jsonObject-key-3", JSONObject().apply {
                put("jsonObject-jsonObject-key-1", "jsonObject-jsonObject-value-1")
                put("jsonObject-jsonObject-key-2", "jsonObject-jsonObject-value-2")
            })
        }

        assertThat(firstJSONObject)
            .usingComparator { p0, p1 ->
                if (p0!!.isEqualTo(p1!!)) 0 else 1
            }
            .isNotEqualTo(secondJSONObject)
    }

    @Test
    fun `isEqualTo if not equal since jsonObject keys differ`() {
        val firstJSONObject = JSONObject().apply {
            put("jsonArray", JSONArray().apply {
                put(1)
                put("string")
                put(JSONObject().apply {
                    put("jsonArray-jsonObject-key-1", "jsonArray-jsonObject-value-1")
                    put("jsonArray-jsonObject-key-2", "jsonArray-jsonObject-value-2")
                })
            })
            put("jsonObject-key-1", "jsonObject-value-1")
            put("jsonObject-key-2", 42)
            put("jsonObject-key-3", JSONObject().apply {
                put("jsonObject-jsonObject-key-1", "jsonObject-jsonObject-value-1")
                put("jsonObject-jsonObject-key-2", "jsonObject-jsonObject-value-2")
            })
        }
        val secondJSONObject = JSONObject().apply {
            put("jsonObject-key-1", "jsonObject-value-1")
            put("jsonObject-key-2", 42)
            put("jsonObject-key-3", JSONObject().apply {
                put("jsonObject-jsonObject-key-1", "jsonObject-jsonObject-value-1")
                put("jsonObject-jsonObject-key-2", "jsonObject-jsonObject-value-2")
            })
        }

        assertThat(firstJSONObject)
            .usingComparator { p0, p1 ->
                if (p0!!.isEqualTo(p1!!)) 0 else 1
            }
            .isNotEqualTo(secondJSONObject)
    }

    @Test
    fun testOptHexByteArray() {
        val jsonObject = JSONObject()
        jsonObject.put("key", "1116aa")
        assertThat(jsonObject.optHexByteArray("key", null)).isEqualTo(byteArrayOf(17, 22, -86))
    }

    @Test
    @Throws(Exception::class)
    fun testOptHexByteArrayIfNotExists() {
        val jsonObject = JSONObject()
        assertThat(jsonObject.optHexByteArray("key", null)).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testOnHexByteArrayIfContainsEmptyString() {
        val jsonObject = JSONObject().put("key", "")
        assertThat(jsonObject.optHexByteArray("key", null)).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun testOptHexByteArrayForInvalidHexString() {
        val jsonObject = JSONObject().put("key", "1a5")
        assertThat(jsonObject.optHexByteArray("key", null)).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testOptHexByteArrayForNonNullFallback() {
        assertThat(JSONObject().optHexByteArray("key", byteArrayOf(1, 2, 3)))
            .isEqualTo(byteArrayOf(1, 2, 3))
    }
}
