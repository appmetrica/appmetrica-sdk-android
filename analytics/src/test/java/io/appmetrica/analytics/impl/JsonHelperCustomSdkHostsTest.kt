package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

class JsonHelperCustomSdkHostsTest : CommonTest() {

    @Test
    fun customSdkHostsToStringNullMap() {
        assertThat(JsonHelper.customSdkHostsToString(null)).isNull()
    }

    @Test
    fun customSdkHostsToStringEmptyMap() {
        assertThat(JsonHelper.customSdkHostsToString(emptyMap())).isEqualTo("{}")
    }

    @Test
    fun customSdkHostsToStringSingleElement() {
        val map = mapOf("host1" to listOf("aaa", "bbb"))
        JSONAssert.assertEquals(
            JSONObject().put("host1", JSONArray().put("aaa").put("bbb").toString()).toString(),
            JsonHelper.customSdkHostsToString(map),
            true
        )
    }

    @Test
    fun customSdkHostsToStringMultipleElements() {
        val map = mapOf("host1" to listOf("aaa", "bbb"), "host2" to emptyList(), "host3" to listOf("ccc"))
        JSONAssert.assertEquals(
            JSONObject()
                .put("host1", JSONArray().put("aaa").put("bbb").toString())
                .put("host3", JSONArray().put("ccc").toString()).toString(),
            JsonHelper.customSdkHostsToString(map),
            true
        )
    }

    @Test
    fun customSdkHostsToStringMultipleElementsThereAndBackAgain() {
        val map = mapOf("host1" to listOf("aaa", "bbb"), "host3" to listOf("ccc"))
        val actual = JsonHelper.customSdkHostsToString(map)
        JSONAssert.assertEquals(
            JSONObject()
                .put("host1", JSONArray().put("aaa").put("bbb").toString())
                .put("host3", JSONArray().put("ccc").toString()).toString(),
            actual,
            true
        )
        assertThat(JsonHelper.customSdkHostsFromString(actual)).isEqualTo(map)
    }

    @Test
    fun customSdkHostsFromStringNull() {
        assertThat(JsonHelper.customSdkHostsFromString(null)).isNull()
    }

    @Test
    fun customSdkHostsFromStringEmpty() {
        assertThat(JsonHelper.customSdkHostsFromString("")).isEmpty()
    }

    @Test
    fun customSdkHostsFromStringNotAJson() {
        assertThat(JsonHelper.customSdkHostsFromString("aaa")).isEmpty()
    }

    @Test
    fun customSdkHostsFromStringEmptyJson() {
        assertThat(JsonHelper.customSdkHostsFromString("{}")).isEmpty()
    }

    @Test
    fun customSdkHostsFromStringSingleValidElement() {
        val input = JSONObject().put("host1", JSONArray().put("aaa").put("bbb").toString()).toString()
        val expected = mapOf("host1" to listOf("aaa", "bbb"))
        assertThat(JsonHelper.customSdkHostsFromString(input)).isEqualTo(expected)
    }

    @Test
    fun customSdkHostsFromStringMultipleValidElements() {
        val input = JSONObject()
            .put("host1", JSONArray().put("aaa").put("bbb").toString())
            .put("host2", JSONArray().toString())
            .put("host3", JSONArray().put("ccc").toString())
            .toString()
        val expected = mapOf("host1" to listOf("aaa", "bbb"), "host3" to listOf("ccc"))
        assertThat(JsonHelper.customSdkHostsFromString(input)).isEqualTo(expected)
    }

    @Test
    fun customSdkHostsFromStringOneElementIsInvalid() {
        val input = JSONObject()
            .put("host1", JSONArray().put("aaa").put("bbb").toString())
            .put("host2", "not an array")
            .put("host3", JSONArray().put("ccc").toString())
            .toString()
        val expected = mapOf("host1" to listOf("aaa", "bbb"), "host3" to listOf("ccc"))
        assertThat(JsonHelper.customSdkHostsFromString(input)).isEqualTo(expected)
    }
}
