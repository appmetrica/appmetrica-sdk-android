package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.modulesapi.internal.common.ModuleSelfReporter
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
    private val eventValue = "Some event value"

    private val selfReporter: ModuleSelfReporter = mock()
    private val serviceContext: ServiceContext = mock {
        on { selfReporter } doReturn selfReporter
    }

    @get:Rule
    val eventValueComposerRule = constructionRule<IdSyncEventValueComposer> {
        on { compose(requestResult) } doReturn eventValue
    }

    private val reporter by setUp { IdSyncEventReporter(serviceContext) }

    @Test
    fun reportEvent() {
        reporter.reportEvent(requestResult)
        verify(selfReporter).reportEvent("id_sync", eventValue)
    }
}
