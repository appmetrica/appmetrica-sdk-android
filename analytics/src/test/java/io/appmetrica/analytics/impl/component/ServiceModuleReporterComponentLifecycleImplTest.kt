package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentContext
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycleListener
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class ServiceModuleReporterComponentLifecycleImplTest : CommonTest() {

    private val firstListener: ServiceModuleReporterComponentLifecycleListener = mock()
    private val secondListener: ServiceModuleReporterComponentLifecycleListener = mock()

    private val context: ServiceModuleReporterComponentContext = mock()

    private val lifecycleImpl = ServiceModuleReporterComponentLifecycleImpl()

    @Test
    fun onMainReporterCreated() {
        lifecycleImpl.subscribe(firstListener)
        lifecycleImpl.subscribe(secondListener)
        lifecycleImpl.onMainReporterCreated(context)

        verify(firstListener).onMainReporterCreated(context)
        verify(secondListener).onMainReporterCreated(context)
    }
}
