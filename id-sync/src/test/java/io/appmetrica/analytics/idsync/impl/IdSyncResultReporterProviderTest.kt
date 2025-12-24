package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class IdSyncResultReporterProviderTest : CommonTest() {

    private val reportUrl = "https://example.com/report"

    private val serviceContext: ServiceContext = mock()

    @get:Rule
    val eventReporterRule = constructionRule<IdSyncResultEventReporter>()

    @get:Rule
    val realtimeReporterRule = constructionRule<IdSyncResultRealtimeReporter>()

    private val provider by setUp { IdSyncResultReporterProvider(serviceContext) }

    @Test
    fun `getReporters returns both reporters when both enabled`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn reportUrl
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).hasSize(2)
        assertThat(reporters[0]).isInstanceOf(IdSyncResultEventReporter::class.java)
        assertThat(reporters[1]).isInstanceOf(IdSyncResultRealtimeReporter::class.java)

        assertThat(eventReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(eventReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext)

        assertThat(realtimeReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(realtimeReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext, reportUrl)
    }

    @Test
    fun `getReporters returns only event reporter when reportEventEnabled true and reportUrl null`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn null
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).hasSize(1)
        assertThat(reporters[0]).isInstanceOf(IdSyncResultEventReporter::class.java)

        assertThat(eventReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(realtimeReporterRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun `getReporters returns only event reporter when reportEventEnabled true and reportUrl empty`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn ""
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).hasSize(1)
        assertThat(reporters[0]).isInstanceOf(IdSyncResultEventReporter::class.java)

        assertThat(eventReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(realtimeReporterRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun `getReporters returns only realtime reporter when reportEventEnabled false and reportUrl set`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn false
            on { reportUrl } doReturn reportUrl
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).hasSize(1)
        assertThat(reporters[0]).isInstanceOf(IdSyncResultRealtimeReporter::class.java)

        assertThat(eventReporterRule.constructionMock.constructed()).isEmpty()
        assertThat(realtimeReporterRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun `getReporters returns empty list when both disabled`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn false
            on { reportUrl } doReturn null
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).isEmpty()

        assertThat(eventReporterRule.constructionMock.constructed()).isEmpty()
        assertThat(realtimeReporterRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun `getReporters returns empty list when reportEventEnabled false and reportUrl empty`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn false
            on { reportUrl } doReturn ""
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).isEmpty()

        assertThat(eventReporterRule.constructionMock.constructed()).isEmpty()
        assertThat(realtimeReporterRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun `getReporters can be called multiple times with different configs`() {
        val configBothEnabled: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn reportUrl
        }

        val configOnlyEvent: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn null
        }

        val configOnlyRealtime: RequestConfig = mock {
            on { reportEventEnabled } doReturn false
            on { reportUrl } doReturn reportUrl
        }

        // First call - both enabled
        val reporters1 = provider.getReporters(configBothEnabled)
        assertThat(reporters1).hasSize(2)

        // Second call - only event
        val reporters2 = provider.getReporters(configOnlyEvent)
        assertThat(reporters2).hasSize(1)
        assertThat(reporters2[0]).isInstanceOf(IdSyncResultEventReporter::class.java)

        // Third call - only realtime
        val reporters3 = provider.getReporters(configOnlyRealtime)
        assertThat(reporters3).hasSize(1)
        assertThat(reporters3[0]).isInstanceOf(IdSyncResultRealtimeReporter::class.java)

        // Verify total constructions
        assertThat(eventReporterRule.constructionMock.constructed()).hasSize(2)
        assertThat(realtimeReporterRule.constructionMock.constructed()).hasSize(2)
    }

    @Test
    fun `getReporters passes correct serviceContext to event reporter`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn null
        }

        provider.getReporters(requestConfig)

        assertThat(eventReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext)
    }

    @Test
    fun `getReporters passes correct parameters to realtime reporter`() {
        val customReportUrl = "https://custom.example.com/report"
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn false
            on { reportUrl } doReturn customReportUrl
        }

        provider.getReporters(requestConfig)

        assertThat(realtimeReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext, customReportUrl)
    }

    @Test
    fun `getReporters returns reporters in correct order`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn true
            on { reportUrl } doReturn reportUrl
        }

        val reporters = provider.getReporters(requestConfig)

        // Event reporter should come first
        assertThat(reporters[0]).isInstanceOf(IdSyncResultEventReporter::class.java)
        // Realtime reporter should come second
        assertThat(reporters[1]).isInstanceOf(IdSyncResultRealtimeReporter::class.java)
    }

    @Test
    fun `getReporters with reportUrl containing only whitespace`() {
        val requestConfig: RequestConfig = mock {
            on { reportEventEnabled } doReturn false
            on { reportUrl } doReturn "   "
        }

        val reporters = provider.getReporters(requestConfig)

        assertThat(reporters).isEmpty()
    }
}
