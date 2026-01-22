package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.json.JSONObject
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions
import org.skyscreamer.jsonassert.JSONAssert

internal class DefaultClientConfigAdditionalFieldsSerializerTest : CommonTest() {

    private val builder: AppMetricaConfig.Builder = mock()

    private val serializer by setUp { DefaultClientConfigAdditionalFieldsSerializer() }

    @Test
    fun toJson() {
        JSONAssert.assertEquals(serializer.toJson(mapOf("first" to "second")), JSONObject(), true)
    }

    @Test
    fun parseJson() {
        val json = mock<JSONObject>()
        serializer.parseJson(json, builder)
        verifyNoInteractions(json, builder)
    }
}
