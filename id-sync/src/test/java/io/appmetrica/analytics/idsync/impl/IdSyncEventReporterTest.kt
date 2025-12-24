package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class IdSyncEventReporterTest : CommonTest() {

    private val requestResult: RequestResult = mock()
    private val requestConfig: RequestConfig = mock()
    private val sdkIdentifiers: SdkIdentifiers = mock()
    private val eventValue = "Some event value"

    private val serviceContext: ServiceContext = mock()

    private val resultReporter: IdSyncResultReporter = mock()

    @get:Rule
    val eventValueComposerRule = constructionRule<IdSyncResultStringComposer> {
        on { compose(requestResult) } doReturn eventValue
    }

    @get:Rule
    val reporterProviderRule = constructionRule<IdSyncResultReporterProvider> {
        on { getReporters(requestConfig) } doReturn listOf(resultReporter)
    }

    private val reporter by setUp { IdSyncResultHandler(serviceContext) }

    @Test
    fun reportEvent() {
        reporter.reportEvent(requestResult, requestConfig, sdkIdentifiers)
        verify(resultReporter).reportResult(eventValue, sdkIdentifiers)
    }
}
