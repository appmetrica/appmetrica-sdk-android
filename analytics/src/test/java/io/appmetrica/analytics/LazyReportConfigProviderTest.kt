package io.appmetrica.analytics

import io.appmetrica.analytics.impl.LazyReportConfigProvider
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions

class LazyReportConfigProviderTest : CommonTest() {

    private val reportRequestConfig = mock<ReportRequestConfig>()
    private val componentUnit = mock<ComponentUnit>() {
        on { freshReportRequestConfig } doReturn reportRequestConfig
    }

    @Test
    fun getConfig() {
        val configProvider = LazyReportConfigProvider(componentUnit)
        verifyNoMoreInteractions(componentUnit)
        assertThat(configProvider.config).isEqualTo(reportRequestConfig)
    }

}
