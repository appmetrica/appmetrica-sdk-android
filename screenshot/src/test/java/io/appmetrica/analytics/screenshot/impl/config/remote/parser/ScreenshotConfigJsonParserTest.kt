package io.appmetrica.analytics.screenshot.impl.config.remote.parser

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.testutils.CommonTest
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ScreenshotConfigJsonParserTest : CommonTest() {

    private val blockJson = JSONObject()
        .put("block", "block")

    private val apiCaptorConfigProto: ApiCaptorConfigProto = mock()
    private val apiCaptorConfigJsonParser: ApiCaptorConfigJsonParser = mock {
        on { parse(blockJson) } doReturn apiCaptorConfigProto
    }

    private val serviceCaptorConfigProto: ServiceCaptorConfigProto = mock()
    private val serviceCaptorConfigJsonParser: ServiceCaptorConfigJsonParser = mock {
        on { parse(blockJson) } doReturn serviceCaptorConfigProto
    }

    private val contentObserverCaptorConfigProto: ContentObserverCaptorConfigProto = mock()
    private val contentObserverConfigJsonParser: ContentObserverCaptorConfigJsonParser = mock {
        on { parse(blockJson) } doReturn contentObserverCaptorConfigProto
    }

    private val parser = ScreenshotConfigJsonParser(
        apiCaptorConfigJsonParser,
        serviceCaptorConfigJsonParser,
        contentObserverConfigJsonParser,
    )

    @Test
    fun parse() {
        val rawData = JSONObject()
            .put("screenshot", blockJson)
        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("apiCaptorConfig", apiCaptorConfigProto)
            .checkField("serviceCaptorConfig", serviceCaptorConfigProto)
            .checkField("contentObserverCaptorConfig", contentObserverCaptorConfigProto)
            .checkAll()
    }

    @Test
    fun parseIfNoValues() {
        val rawData = JSONObject()
            .put("screenshot", JSONObject())
        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkFieldsAreNull(
                "apiCaptorConfig",
                "serviceCaptorConfig",
                "contentObserverCaptorConfig",
            )
            .checkAll()
    }

    @Test
    fun parseIfNoBlock() {
        val rawData = JSONObject()

        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkFieldComparingFieldByFieldRecursively("apiCaptorConfig", ApiCaptorConfigProto())
            .checkFieldComparingFieldByFieldRecursively("serviceCaptorConfig", ServiceCaptorConfigProto())
            .checkFieldComparingFieldByFieldRecursively(
                "contentObserverCaptorConfig",
                ContentObserverCaptorConfigProto().also {
                    it.mediaStoreColumnNames = Constants.Defaults.defaultMediaStoreColumnNames
                }
            )
            .checkAll()
    }
}
