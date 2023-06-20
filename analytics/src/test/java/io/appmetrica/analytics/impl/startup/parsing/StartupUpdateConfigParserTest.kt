package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.db.state.converter.StartupUpdateConfigConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class StartupUpdateConfigParserTest : CommonTest() {

    private val model = mock<StartupUpdateConfig>()
    private val nanoCaptor = argumentCaptor<StartupStateProtobuf.StartupState.StartupUpdateConfig>()
    private val converter = mock<StartupUpdateConfigConverter> {
        on { toModel(nanoCaptor.capture()) } doReturn model
    }
    private val parser = StartupUpdateConfigParser(converter)

    @Test
    fun noStartupUpdateBlock() {
        val result = StartupResult()
        parser.parse(result, JSONObject())
        assertThat(result.startupUpdateConfig).isSameAs(model)
        ProtoObjectPropertyAssertions(nanoCaptor.firstValue)
            .checkField("interval", 86400)
            .checkAll()
    }

    @Test
    fun emptyStartupUpdateBlock() {
        val result = StartupResult()
        parser.parse(result, JSONObject().put("startup_update", JSONObject()))
        assertThat(result.startupUpdateConfig).isSameAs(model)
        ProtoObjectPropertyAssertions(nanoCaptor.firstValue)
            .checkField("interval", 86400)
            .checkAll()
    }

    @Test
    fun filledStartupUpdateBlock() {
        val result = StartupResult()
        parser.parse(result, JSONObject().put("startup_update", JSONObject().put("interval_seconds", 777888)))
        assertThat(result.startupUpdateConfig).isSameAs(model)
        ProtoObjectPropertyAssertions(nanoCaptor.firstValue)
            .checkField("interval", 777888)
            .checkAll()
    }
}
