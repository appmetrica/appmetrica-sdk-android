package io.appmetrica.analytics.impl

import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.RandomStringGenerator
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.robolectric.RobolectricTestRunner
import java.util.Random

@RunWith(RobolectricTestRunner::class)
class JsonHelperOldApiTest : CommonTest() {

    @get:Rule
    val androidUtilsMockedStaticRule = staticRule<AndroidUtils> {
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT) } doReturn false
    }

    private val testObject = Any()

    @Test
    fun mapToJsonStringForSimpleMap() {
        val eventMap = mapOf(
            "string" to "value",
            "int" to 111,
            "bool" to false,
            "double" to 22.444,
            "object" to testObject
        )
        val json = JsonHelper.mapToJsonString(eventMap)
        assertThat(json.toString()).isEqualTo(JSONObject(eventMap).toString())
    }

    @Test
    fun mapToJsonStringWithNestedMap() {
        val innerMap = mapOf("inner" to "value", "inner2" to 22.5)
        val eventMap = mapOf("string" to "value", "object" to testObject, "additional" to innerMap)
        val jsonObject = JsonHelper.prepareForJson(eventMap) as JSONObject
        assertThat(jsonObject["additional"]).isExactlyInstanceOf(JSONObject::class.java)
        assertThat(jsonObject.getString("object")).isEqualTo(testObject.toString())
        assertThat(jsonObject["string"]).isEqualTo("value")
        val innerJson = jsonObject.getJSONObject("additional")
        assertThat(innerJson.getString("inner")).isEqualTo("value")
        assertThat(innerJson.getDouble("inner2")).isEqualTo(22.5)
    }

    @Test
    fun mapToJsonStringWithNestedArray() {
        val innerArray = listOf<Boolean?>(false, true)
        val innerArray2 = intArrayOf(1, 11, 111, 1111)
        val eventMap = mapOf("bool_array" to innerArray, "int_array" to innerArray2)

        val jsonObject = JsonHelper.prepareForJson(eventMap) as JSONObject
        val boolArray = jsonObject["bool_array"] as JSONArray
        assertThat(boolArray[0]).isEqualTo(false)
        assertThat(boolArray[1]).isEqualTo(true)
        assertThat(boolArray.length()).isEqualTo(2)
        val intArray = jsonObject.getJSONArray("int_array")
        for (i in 0 until intArray.length()) {
            assertThat(intArray.getInt(i)).isEqualTo(innerArray2[i])
        }
        assertThat(intArray.length()).isEqualTo(4)
    }

    @Test
    fun mapToJsonStringWithNestedArrayAndMap() {
        val innerMap = mapOf("inner" to "value", "inner2" to 22.5)
        val innerArray2 = intArrayOf(1, 11, 111, 1111)
        val eventMap = mapOf("string" to "value", "map" to innerMap, "int_array" to innerArray2)
        val jsonObject = JsonHelper.prepareForJson(eventMap) as JSONObject

        assertThat(jsonObject["map"]).isExactlyInstanceOf(JSONObject::class.java)
        val intArray = jsonObject.getJSONArray("int_array")

        for (i in 0 until intArray.length()) {
            assertThat(intArray.getInt(i)).isEqualTo(innerArray2[i])
        }
        assertThat(intArray.length()).isEqualTo(4)
        val jsonMap = jsonObject.getJSONObject("map")
        assertThat(jsonMap.length()).isEqualTo(2)
        assertThat(jsonMap.getString("inner")).isEqualTo("value")
        assertThat(jsonMap.getDouble("inner2")).isEqualTo(22.5)
    }

    @Test
    fun mapToJsonStringWithDoubleNestedMap() {
        val doubleInnerMap = mapOf("inner-inner" to testObject, "inner-inner2" to 22.5)
        val innerMap = mapOf("inner" to "value", "innerMap" to doubleInnerMap)
        val innerArray1 = arrayOf(false, true, null)
        val innerArray2 = intArrayOf(1, 11, 111, 1111)
        val eventMap = mapOf(
            "string" to "value",
            "map" to innerMap,
            "bool_array" to innerArray1,
            "int_array" to innerArray2
        )
        val jsonObject = JsonHelper.prepareForJson(eventMap) as JSONObject
        val innerJsonMap = jsonObject.getJSONObject("map")
        assertThat(innerJsonMap.getString("inner")).isEqualTo("value")
        val doubleInnerJsonMap = innerJsonMap.getJSONObject("innerMap")
        assertThat(doubleInnerJsonMap.getString("inner-inner")).isEqualTo(testObject.toString())
        assertThat(doubleInnerJsonMap.getDouble("inner-inner2")).isEqualTo(22.5)
    }

    @Test
    fun prepareJsonForMapWithStringShouldReturnTheSameValueAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to randomString()))
    }

    @Test
    fun prepareJsonForMapWithNullShouldReturnTheSameValueAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to null))
    }

    @Test
    fun prepareJsonForMapWithIntShouldReturnTheSameValueAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to Random().nextInt()))
    }

    @Test
    fun prepareJsonForMapWithLongShouldReturnTheSameValueAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to Random().nextLong()))
    }

    @Test
    fun prepareJsonForMapWithFloatShouldReturnTheSameValuesAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to Random().nextFloat()))
    }

    @Test
    fun prepareJsonWithDoubleShouldReturnTheSameValueAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to Random().nextDouble()))
    }

    @Test
    fun prepareJsonWithBooleanShouldReturnTheSameValueAsJsonObject() {
        assertPrepareJsonIsEqualsJsonObject(mapOf(randomString() to Random().nextBoolean()))
    }

    @Test
    fun prepareJsonForMapWithStringArrayShouldReturnCorrectJson() {
        assertThat(JsonHelper.prepareForJson(mapOf("array" to arrayOf("string"))).toString())
            .isEqualTo("{\"array\":[\"string\"]}")
    }

    @Test
    fun prepareJsonForMapWithArrayListWithStringShouldReturnCorrectJson() {
        assertThat(JsonHelper.prepareForJson(mapOf("array" to listOf("string"))).toString())
            .isEqualTo("{\"array\":[\"string\"]}")
    }

    @Test
    fun prepareJsonForMapWithHashSetWithStringShouldReturnCorrectJson() {
        assertThat(JsonHelper.prepareForJson(mapOf("array" to setOf("string"))).toString())
            .isEqualTo("{\"array\":[\"string\"]}")
    }

    @Test
    fun prepareJsonForMapWithMapWithStringShouldReturnCorrectJson() {
        assertThat(JsonHelper.prepareForJson(mapOf("array" to mapOf("key" to "value"))).toString())
            .isEqualTo("{\"array\":{\"key\":\"value\"}}")
    }

    private fun assertPrepareJsonIsEqualsJsonObject(map: Map<out Any?, Any?>) {
        val actualValue = JsonHelper.prepareForJson(HashMap(map)).toString()
        val expectedValue = JSONObject(HashMap(map)).toString()
        assertThat(actualValue).isEqualTo(expectedValue)
    }

    private fun randomString(): String {
        val randomStringGenerator = RandomStringGenerator(Random().nextInt(30) + 1)
        return randomStringGenerator.nextString()
    }
}
