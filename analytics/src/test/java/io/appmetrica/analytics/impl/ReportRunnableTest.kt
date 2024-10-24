package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.impl.client.ClientConfiguration
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.component.clients.ClientRepository
import io.appmetrica.analytics.impl.component.clients.ClientUnit
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReportRunnableTest : CommonTest() {

    private val context: Context = mock()
    private val counterReport: CounterReport = mock()
    private val extras: Bundle = mock()
    private val clientDescription: ClientDescription = mock()
    private val clientUnit: ClientUnit = mock()
    private val commonArgumentsCaptor = argumentCaptor<CommonArguments>()
    private val reporter: IReporterExtended = mock()

    @get:Rule
    val selfReportFacadeMockedStaticRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn reporter
    }

    @get:Rule
    val commonArgumentsMockedConstructionRule = constructionRule<CommonArguments>()

    private val commonArguments: CommonArguments by commonArgumentsMockedConstructionRule

    private val clientRepository: ClientRepository = mock {
        on { getOrCreateClient(eq(clientDescription), any()) } doReturn clientUnit
    }

    private val appVersionName = "App version name"
    private val appBuildNumber = "App build number"
    private val deviceType = "Device type"

    private val reporterConfiguration: CounterConfiguration = mock {
        on { appVersion } doReturn appVersionName
        on { appBuildNumber } doReturn appBuildNumber
        on { deviceType } doReturn deviceType
    }

    private val clientConfiguration: ClientConfiguration = mock {
        on { reporterConfiguration } doReturn reporterConfiguration
    }

    @get:Rule
    val clientConfigurationMockedStaticRule = staticRule<ClientConfiguration> {
        on { ClientConfiguration.fromBundle(context, extras) } doReturn clientConfiguration
    }

    @get:Rule
    val clientDescriptionMockedStaticRule = staticRule<ClientDescription> {
        on { ClientDescription.fromClientConfiguration(clientConfiguration) } doReturn clientDescription
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val reportRunnable by setUp {
        ReportRunnable(context, counterReport, extras, clientRepository)
    }

    @Test
    fun run() {
        reportRunnable.run()

        val sdkEnvironmentHolder = GlobalServiceLocator.getInstance().sdkEnvironmentHolder

        inOrder(sdkEnvironmentHolder, clientUnit) {
            verify(sdkEnvironmentHolder).mayBeUpdateAppVersion(appVersionName, appBuildNumber)
            verify(sdkEnvironmentHolder).mayBeUpdateDeviceTypeFromClient(deviceType)
            verify(clientUnit).handle(eq(counterReport), commonArgumentsCaptor.capture())
            verifyNoMoreInteractions()
        }

        assertThat(commonArgumentsCaptor.firstValue).isSameAs(commonArguments)
    }

    @Test
    fun `run if config is null`() {
        whenever(ClientConfiguration.fromBundle(context, extras)).thenReturn(null)
        reportRunnable.run()
        verifyNoInteractions(GlobalServiceLocator.getInstance().sdkEnvironmentHolder, clientUnit)
    }

    @Test
    fun `run if exception is thrown`() {
        val type = 100500
        val customType = 200500
        whenever(counterReport.type).thenReturn(type)
        whenever(counterReport.customType).thenReturn(customType)
        whenever(clientUnit.handle(any(), any())).thenThrow(RuntimeException())
        reportRunnable.run()
        verify(reporter).reportError(
            argThat<String> { startsWith("Exception during processing event with type: $type ($customType)") },
            argThat<Throwable> { this is RuntimeException }
        )
    }
}
