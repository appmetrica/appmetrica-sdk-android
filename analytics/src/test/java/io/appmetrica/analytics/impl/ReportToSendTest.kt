package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.ClientCounterReport.TrimmedField
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
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

    @Test
    fun builder() {
        val counterReport = mock<CounterReport>()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment).build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(AppMetricaServiceDataReporter.TYPE_CORE)
            assertThat(reportToSend.trimmedFields).isNull()
        }.assertAll()
    }

    @Test
    fun builderAsCrash() {
        val counterReport = mock<CounterReport>()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .asCrash(true)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isTrue
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(AppMetricaServiceDataReporter.TYPE_CORE)
            assertThat(reportToSend.trimmedFields).isNull()
        }.assertAll()
    }

    @Test
    fun builderWithTrimmerFields() {
        val counterReport = mock<CounterReport>()
        val trimmedField = mock<HashMap<TrimmedField, Int>>()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .withTrimmedFields(trimmedField)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(AppMetricaServiceDataReporter.TYPE_CORE)
            assertThat(reportToSend.trimmedFields).isSameAs(trimmedField)
        }.assertAll()
    }

    @Test
    fun builderWithServiceDataReporterType() {
        val counterReport = mock<CounterReport>()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .withServiceDataReporterType(42)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.serviceDataReporterType).isEqualTo(42)
            assertThat(reportToSend.trimmedFields).isNull()
        }.assertAll()
    }

    @Test
    fun initialUserProfileID() {
        val userProfileID = "user_profile_id"
        whenever(reporterEnvironment.initialUserProfileID).thenReturn(userProfileID)
        val reportToSend = ReportToSend.newBuilder(
            mock(),
            reporterEnvironment
        ).build()
        assertThat(reportToSend.environment.initialUserProfileID).isEqualTo(userProfileID)
    }
}
