package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NativeCrashConsumerTest : CommonTest() {

    private val reportConsumer: ReportConsumer = mock()

    private val apiKey = "Api key"
    private val packageName = "package name"
    private val processID = 100500
    private val processSessionId = "process session id"
    private val reporterType = CounterConfigurationReporterType.CRASH
    private val metaData: AppMetricaNativeCrashMetadata = mock {
        on { apiKey } doReturn apiKey
        on { packageName } doReturn packageName
        on { processID } doReturn processID
        on { processSessionID } doReturn processSessionId
        on { reporterType } doReturn reporterType
    }

    private val nativeCrashDump = "Native crash dump"

    private val counterReport: CounterReport = mock()
    private val reportCreator: NativeCrashReportCreator = mock {
        on { create(nativeCrashDump) } doReturn counterReport
    }

    @get:Rule
    val clientDescriptionMockedConstructionRule = constructionRule<ClientDescription>()
    private val clientDescription: ClientDescription by clientDescriptionMockedConstructionRule

    @get:Rule
    val commonArgumentsMockedConstructionRule = constructionRule<CommonArguments>()
    private val commonArguments: CommonArguments by commonArgumentsMockedConstructionRule

    @get:Rule
    val startupRequestConfigArgumentsMockedConstructionRule = constructionRule<StartupRequestConfig.Arguments>()
    private val startupRequestConfigArguments: StartupRequestConfig.Arguments
        by startupRequestConfigArgumentsMockedConstructionRule

    @get:Rule
    val reporterArgumentsMockedConstructionRule = constructionRule<ReporterArguments>()
    private val reporterArguments: ReporterArguments by reporterArgumentsMockedConstructionRule

    private val nativeCrashConsumer: NativeCrashConsumer by setUp {
        NativeCrashConsumer(reportConsumer, metaData, reportCreator)
    }

    @Test
    fun consume() {
        nativeCrashConsumer.consume(nativeCrashDump)
        verify(reportConsumer).consumeCrash(clientDescription, counterReport, commonArguments)
    }

    @Test
    fun `consume - check client description`() {
        nativeCrashConsumer.consume(nativeCrashDump)

        assertThat(clientDescriptionMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(clientDescriptionMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(apiKey, packageName, processID, processSessionId, reporterType)
    }

    @Test
    fun `consume - check common arguments`() {
        nativeCrashConsumer.consume(nativeCrashDump)
        assertThat(commonArgumentsMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(commonArgumentsMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(startupRequestConfigArguments, reporterArguments, null)

        assertThat(startupRequestConfigArgumentsMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupRequestConfigArgumentsMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()

        assertThat(reporterArgumentsMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterArgumentsMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
