package io.appmetrica.analytics.impl

import android.content.Context
import android.os.ResultReceiver
import io.appmetrica.analytics.impl.ClientCounterReport.TrimmedField
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReportToSendTest : CommonTest() {

    @Test
    fun builder() {
        val counterReport = mock<CounterReport>()
        val reporterEnvironment = ReporterEnvironmentTest.createStubbedEnvironment()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment).build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.metricaServiceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(reportToSend.trimmedFields).isNull()
        }.assertAll()
    }

    @Test
    fun builderAsCrash() {
        val counterReport = mock<CounterReport>()
        val reporterEnvironment = ReporterEnvironmentTest.createStubbedEnvironment()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .asCrash(true)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isTrue
            assertThat(reportToSend.metricaServiceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(reportToSend.trimmedFields).isNull()
        }.assertAll()
    }

    @Test
    fun builderWithTrimmerFields() {
        val counterReport = mock<CounterReport>()
        val reporterEnvironment = ReporterEnvironmentTest.createStubbedEnvironment()
        val trimmedField = mock<HashMap<TrimmedField, Int>>()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .withTrimmedFields(trimmedField)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.metricaServiceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(reportToSend.trimmedFields).isSameAs(trimmedField)
        }.assertAll()
    }

    @Test
    fun builderWithMetricaServiceDataReporterType() {
        val counterReport = mock<CounterReport>()
        val reporterEnvironment = ReporterEnvironmentTest.createStubbedEnvironment()
        val reportToSend = ReportToSend.newBuilder(counterReport, reporterEnvironment)
            .withMetricaServiceDataReporterType(42)
            .build()

        SoftAssertions().apply {
            assertThat(reportToSend.report).isSameAs(counterReport)
            assertThat(reportToSend.isCrashReport).isFalse
            assertThat(reportToSend.metricaServiceDataReporterType).isEqualTo(42)
            assertThat(reportToSend.trimmedFields).isNull()
        }.assertAll()
    }

    @Test
    fun initialUserProfileID() {
        val userProfileID = "user_profile_id"
        val reporterEnvironment = mock<ReporterEnvironment>()
        val processConfiguration = ProcessConfiguration(mock<Context>(), mock<ResultReceiver>())
        val counterConfiguration = CounterConfiguration()
        whenever(reporterEnvironment.processConfiguration).thenReturn(processConfiguration)
        whenever(reporterEnvironment.reporterConfiguration).thenReturn(counterConfiguration)
        whenever(reporterEnvironment.initialUserProfileID).thenReturn(userProfileID)
        val reportToSend = ReportToSend.newBuilder(
            mock(),
            reporterEnvironment
        ).build()
        assertThat(reportToSend.environment.initialUserProfileID).isEqualTo(userProfileID)
    }
}
