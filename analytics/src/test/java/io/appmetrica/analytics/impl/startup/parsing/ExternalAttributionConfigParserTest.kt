package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import java.util.concurrent.TimeUnit

internal class ExternalAttributionConfigParserTest : CommonTest() {

    private val parser = ExternalAttributionConfigParser()
    private val result = StartupResult()

    @Test
    fun noBlock() {
        val startupJsonMock = StartupJsonMock()
        parser.parse(result, startupJsonMock)
        ObjectPropertyAssertions(result.externalAttributionConfig).apply {
            checkField("collectingInterval", 864000000L)
            checkAll()
        }
    }

    @Test
    fun hasBlockNoData() {
        val startupJsonMock = StartupJsonMock()
        startupJsonMock.addEmptyExternalAttributionConfig()
        parser.parse(result, startupJsonMock)
        ObjectPropertyAssertions(result.externalAttributionConfig).apply {
            checkField("collectingInterval", 864000000L)
            checkAll()
        }
    }

    @Test
    fun hasData() {
        val collectingIntervalSeconds = 1L
        val startupJsonMock = StartupJsonMock()
        startupJsonMock.addExternalAttributionConfig(collectingIntervalSeconds)
        parser.parse(result, startupJsonMock)
        ObjectPropertyAssertions(result.externalAttributionConfig).apply {
            checkField("collectingInterval", TimeUnit.SECONDS.toMillis(collectingIntervalSeconds))
            checkAll()
        }
    }
}
