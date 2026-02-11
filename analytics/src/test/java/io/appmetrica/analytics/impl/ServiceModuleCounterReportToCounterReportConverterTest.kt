package io.appmetrica.analytics.impl

import android.util.Base64
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleCounterReport
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ServiceModuleCounterReportToCounterReportConverterTest : CommonTest() {

    private val type = 12
    private val name = "some name"
    private val value = "some value"
    private val valueBytes = "some value bytes".toByteArray()
    private val encodedValueBytes = "encoded value bytes".toByteArray()

    @get:Rule
    val base64Rule = staticRule<Base64> {
        on { Base64.encode(valueBytes, Base64.DEFAULT) } doReturn encodedValueBytes
    }

    private val report: ServiceModuleCounterReport = mock {
        on { type } doReturn type
        on { name } doReturn name
        on { value } doReturn value
    }

    private val converter = ServiceModuleCounterReportToCounterReportConverter()

    @Test
    fun convert() {
        val counterReport = converter.convert(report)

        assertSoftly {
            it.assertThat(counterReport.type).isEqualTo(type)
            it.assertThat(counterReport.name).isEqualTo(name)
            it.assertThat(counterReport.value).isEqualTo(value)
            it.assertAll()
        }
    }

    @Test
    fun convertIfHasValueBytes() {
        whenever(report.valueBytes).thenReturn(valueBytes)

        val counterReport = converter.convert(report)

        assertSoftly {
            it.assertThat(counterReport.type).isEqualTo(type)
            it.assertThat(counterReport.name).isEqualTo(name)
            it.assertThat(counterReport.value)
                .isEqualTo(String(encodedValueBytes))
            it.assertAll()
        }
    }
}
