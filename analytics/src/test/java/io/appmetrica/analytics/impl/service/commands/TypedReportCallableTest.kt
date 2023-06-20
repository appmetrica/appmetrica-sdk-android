package io.appmetrica.analytics.impl.service.commands

import android.os.Bundle
import io.appmetrica.analytics.IAppMetricaService
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TypedReportCallableTest : CommonTest() {

    private val appMetricaConnector: AppMetricaConnector = mock()
    private val shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker = mock()
    private val service: IAppMetricaService = mock()
    private val type: Int = 42
    private val bundle: Bundle = mock()

    private lateinit var typedReportCallable: TypedReportCallable

    @Before
    fun setUp() {
        typedReportCallable = TypedReportCallable(
            appMetricaConnector,
            shouldDisconnectFromServiceChecker,
            type,
            bundle
        )
    }

    @Test
    fun reportToService() {
        whenever(appMetricaConnector.service).thenReturn(service)

        typedReportCallable.call()

        verify(service).reportData(type, bundle)
    }
}
