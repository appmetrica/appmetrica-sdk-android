package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ServiceCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock

internal class ScreenshotConfigProtoConverterTest : CommonTest() {

    private val apiCaptorConfig: ApiCaptorConfig = mock()
    private val apiCaptorConfigProto: ApiCaptorConfigProto = mock()
    private val apiCaptorConfigProtoConverter: ApiCaptorConfigProtoConverter = mock {
        on { fromModel(apiCaptorConfig) }.thenReturn(apiCaptorConfigProto)
        on { toModel(apiCaptorConfigProto) }.thenReturn(apiCaptorConfig)
    }

    private val serviceCaptorConfig: ServiceCaptorConfig = mock()
    private val serviceCaptorConfigProto: ServiceCaptorConfigProto = mock()
    private val serviceCaptorConfigProtoConverter: ServiceCaptorConfigProtoConverter = mock {
        on { fromModel(serviceCaptorConfig) }.thenReturn(serviceCaptorConfigProto)
        on { toModel(serviceCaptorConfigProto) }.thenReturn(serviceCaptorConfig)
    }

    private val contentObserverCaptorConfig: ContentObserverCaptorConfig = mock()
    private val contentObserverCaptorConfigProto: ContentObserverCaptorConfigProto = mock()
    private val contentObserverCaptorConfigProtoConverter: ContentObserverCaptorConfigProtoConverter = mock {
        on { fromModel(contentObserverCaptorConfig) }.thenReturn(contentObserverCaptorConfigProto)
        on { toModel(contentObserverCaptorConfigProto) }.thenReturn(contentObserverCaptorConfig)
    }

    private val converter = ScreenshotConfigProtoConverter(
        apiCaptorConfigProtoConverter,
        serviceCaptorConfigProtoConverter,
        contentObserverCaptorConfigProtoConverter
    )

    @Test
    fun fromModel() {
        val value = ScreenshotConfig(
            apiCaptorConfig = apiCaptorConfig,
            serviceCaptorConfig = serviceCaptorConfig,
            contentObserverCaptorConfig = contentObserverCaptorConfig
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("apiCaptorConfig", apiCaptorConfigProto)
            .checkField("serviceCaptorConfig", serviceCaptorConfigProto)
            .checkField("contentObserverCaptorConfig", contentObserverCaptorConfigProto)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = ScreenshotConfigProto().also {
            it.apiCaptorConfig = apiCaptorConfigProto
            it.serviceCaptorConfig = serviceCaptorConfigProto
            it.contentObserverCaptorConfig = contentObserverCaptorConfigProto
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("apiCaptorConfig", apiCaptorConfig)
            .checkField("serviceCaptorConfig", serviceCaptorConfig)
            .checkField("contentObserverCaptorConfig", contentObserverCaptorConfig)
            .checkAll()
    }
}
