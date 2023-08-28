package io.appmetrica.analytics.impl.service.commands

import android.os.Bundle
import io.appmetrica.analytics.internal.IAppMetricaService
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class PauseUserSessionCallableTest : CommonTest() {

    private val appMetricaConnector: AppMetricaConnector = mock()
    private val shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker = mock()
    private val processConfiguration: ProcessConfiguration = mock()
    private val service: IAppMetricaService = mock()

    private val processConfigurationToBundleCaptor = argumentCaptor<Bundle>()

    private lateinit var pauseUserSessionCallable: PauseUserSessionCallable

    @Before
    fun setUp() {
        pauseUserSessionCallable = PauseUserSessionCallable(
            appMetricaConnector,
            shouldDisconnectFromServiceChecker,
            processConfiguration
        )
    }

    @Test
    fun reportToService() {
        whenever(appMetricaConnector.service).thenReturn(service)

        pauseUserSessionCallable.call()

        verify(processConfiguration).toBundle(processConfigurationToBundleCaptor.capture())
        verify(service).pauseUserSession(processConfigurationToBundleCaptor.firstValue)
    }
}
