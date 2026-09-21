package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ReportToSendTest : CommonTest() {

    private val processConfiguration: ProcessConfiguration = mock()
    private val reporterConfiguration: CounterConfiguration = mock()
    private val reporterEnvironment: ReporterEnvironment = mock() {
        on { processConfiguration } doReturn processConfiguration
        on { reporterConfiguration } doReturn reporterConfiguration
    }

    private fun coreClientEvent(): CoreClientEvent = mock {
        on { trimPolicy } doReturn EventTrimPolicy.STANDARD
        on { bytesTruncated } doReturn 0
        on { extras } doReturn mutableMapOf()
    }

    @Test
    fun builder() {
        val counterReport = coreClientEvent()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment).build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isInstanceOf(TrimmedCoreClientEvent::class.java)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(AppMetricaServiceDataReporter.TYPE_CORE)
        }.assertAll()
    }

    @Test
    fun builderAsCrash() {
        val counterReport = coreClientEvent()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .asCrash(true)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isInstanceOf(TrimmedCoreClientEvent::class.java)
            assertThat(reportToSend.isCrashReport).isTrue
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(AppMetricaServiceDataReporter.TYPE_CORE)
        }.assertAll()
    }

    @Test
    fun builderWithServiceDataReporterType() {
        val counterReport = coreClientEvent()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .withServiceDataReporterType(42)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isInstanceOf(TrimmedCoreClientEvent::class.java)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(42)
        }.assertAll()
    }

    @Test
    fun initialUserProfileID() {
        val userProfileID = "user_profile_id"
        whenever(reporterEnvironment.initialUserProfileID).thenReturn(userProfileID)
        val reportToSend = ReportToSend.newBuilder(
            coreClientEvent(),
            reporterEnvironment
        ).build()
        assertThat(reportToSend.environment.initialUserProfileID).isEqualTo(userProfileID)
    }
}
