package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class ScreenshotConfigProtoConverterTest : CommonTest() {

    private val apiCaptorConfig: ServiceSideApiCaptorConfig = mock()
    private val apiCaptorConfigProto: ApiCaptorConfigProto = mock()
    private val apiCaptorConfigProtoConverter: ApiCaptorConfigProtoConverter = mock {
        on { fromModel(apiCaptorConfig) }.thenReturn(apiCaptorConfigProto)
        on { toModel(apiCaptorConfigProto) }.thenReturn(apiCaptorConfig)
    }

    private val serviceCaptorConfig: ServiceSideServiceCaptorConfig = mock()
    private val serviceCaptorConfigProto: ServiceCaptorConfigProto = mock()
    private val serviceCaptorConfigProtoConverter: ServiceCaptorConfigProtoConverter = mock {
        on { fromModel(serviceCaptorConfig) }.thenReturn(serviceCaptorConfigProto)
        on { toModel(serviceCaptorConfigProto) }.thenReturn(serviceCaptorConfig)
    }

    private val contentObserverCaptorConfig: ServiceSideContentObserverCaptorConfig = mock()
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
        val value = ServiceSideScreenshotConfig(
            enabled = true,
            apiCaptorConfig = apiCaptorConfig,
            serviceCaptorConfig = serviceCaptorConfig,
            contentObserverCaptorConfig = contentObserverCaptorConfig,
        )

        val proto = converter.fromModel(value)

        assertThat(proto.enabled).isTrue()
        ProtoObjectPropertyAssertions(proto.config)
            .checkField("apiCaptorConfig", apiCaptorConfigProto)
            .checkField("serviceCaptorConfig", serviceCaptorConfigProto)
            .checkField("contentObserverCaptorConfig", contentObserverCaptorConfigProto)
            .checkAll()
    }

    @Test
    fun toModel() {
        val proto = RemoteScreenshotConfigProto().also { remoteProto ->
            remoteProto.enabled = false
            remoteProto.config = ScreenshotConfigProto().also {
                it.apiCaptorConfig = apiCaptorConfigProto
                it.serviceCaptorConfig = serviceCaptorConfigProto
                it.contentObserverCaptorConfig = contentObserverCaptorConfigProto
            }
        }

        ObjectPropertyAssertions(converter.toModel(proto))
            .checkField("enabled", false)
            .checkField("apiCaptorConfig", apiCaptorConfig)
            .checkField("serviceCaptorConfig", serviceCaptorConfig)
            .checkField("contentObserverCaptorConfig", contentObserverCaptorConfig)
            .checkAll()
    }
}
