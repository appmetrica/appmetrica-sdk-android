package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.modulesapi.internal.common.ModuleSelfReporter
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class IdSyncResultEventReporterTest : CommonTest() {

    private val sdkIdentifiers: SdkIdentifiers = mock()
    private val moduleSelfReporter: ModuleSelfReporter = mock()

    private val serviceContext: ServiceContext = mock {
        on { selfReporter } doReturn moduleSelfReporter
    }

    private val reporter by setUp { IdSyncResultEventReporter(serviceContext) }

    @Test
    fun `reportResult reports event`() {
        val value = "test_value"
        reporter.reportResult(value, sdkIdentifiers)
        verify(moduleSelfReporter).reportEvent("id_sync", value)
    }
}
