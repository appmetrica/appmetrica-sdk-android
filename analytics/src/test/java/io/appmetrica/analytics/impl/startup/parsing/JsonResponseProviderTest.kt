package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.testutils.CommonTest
import org.json.JSONObject
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class JsonResponseProviderTest : CommonTest() {

    private val jsonResponseProvider = JsonResponseProvider()

    @Test(expected = Throwable::class)
    fun emptyInput() {
        jsonResponseProvider.jsonFromBytes(ByteArray(0))
    }

    @Test(expected = Throwable::class)
    fun invalidJsonInput() {
        jsonResponseProvider.jsonFromBytes("787987".toByteArray())
    }

    @Test
    fun emptyJson() {
        val result = jsonResponseProvider.jsonFromBytes("{}".toByteArray())
        JSONAssert.assertEquals(result, JSONObject(), true)
    }

    @Test
    fun nonEmptyJson() {
        val result = jsonResponseProvider.jsonFromBytes("{ \"aaa\" : 11 }".toByteArray())
        JSONAssert.assertEquals(result, JSONObject(mapOf("aaa" to 11)), true)
    }
}
