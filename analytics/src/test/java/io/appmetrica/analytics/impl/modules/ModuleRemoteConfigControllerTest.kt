package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModuleRemoteConfigControllerTest : CommonTest() {

    private val model = Object()
    private val proto = ByteArray(10)
    private val json = JSONObject().put("some key", "some value")
    private val protobufConverter = mock<Converter<Any, ByteArray>> {
        on { fromModel(model) } doReturn proto
        on { toModel(proto) } doReturn model
    }
    private val jsonParser = mock<JsonParser<Any>> {
        on { parseOrNull(json) } doReturn model
    }

    private val configuration = mock<RemoteConfigExtensionConfiguration<Any>> {
        on { getProtobufConverter() } doReturn protobufConverter
        on { getJsonParser() } doReturn jsonParser
    }

    private val moduleRemoteConfigController = ModuleRemoteConfigController(configuration)

    @Test
    fun fromModel() {
        assertThat(moduleRemoteConfigController.fromModel(model)).isEqualTo(proto)
    }

    @Test
    fun toModel() {
        assertThat(moduleRemoteConfigController.toModel(proto)).isEqualTo(model)
    }

    @Test
    fun parse() {
        assertThat(moduleRemoteConfigController.parseOrNull(json)).isEqualTo(model)
    }

}
