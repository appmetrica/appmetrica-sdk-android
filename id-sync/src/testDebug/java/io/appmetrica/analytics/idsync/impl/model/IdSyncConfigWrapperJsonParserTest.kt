package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class IdSyncConfigWrapperJsonParserTest : CommonTest() {

    private val config: IdSyncConfig = mock()
    private val rawData: JSONObject = mock()

    @Test
    fun parseDelegatesAndWraps() {
        val parser: IdSyncConfigParser = mock { on { parse(rawData) } doReturn config }
        val result = IdSyncConfigWrapperJsonParser(parser).parse(rawData)

        assertThat(result.config).isSameAs(config)
    }

    @Test
    fun parseOrNullDelegatesAndWraps() {
        val parser: IdSyncConfigParser = mock { on { parseOrNull(rawData) } doReturn config }
        val result = IdSyncConfigWrapperJsonParser(parser).parseOrNull(rawData)

        assertThat(result?.config).isSameAs(config)
    }

    @Test
    fun parseOrNullReturnsNullWhenDelegateReturnsNull() {
        val parser: IdSyncConfigParser = mock { on { parseOrNull(rawData) } doReturn null }
        val result = IdSyncConfigWrapperJsonParser(parser).parseOrNull(rawData)

        assertThat(result).isNull()
    }
}
