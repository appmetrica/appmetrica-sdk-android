package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ICrashTransformer
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.MainOrCrashReporterProvider
import io.appmetrica.analytics.impl.TestsData
import io.appmetrica.analytics.impl.crash.client.CrashProcessor
import io.appmetrica.analytics.impl.crash.client.ReporterBasedCrashProcessor
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.SoftAssertions
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class ApplicationCrashProcessorCreatorTest : CommonTest() {

    private val reporterFactoryProvider = mock<IReporterFactoryProvider>()
    private val context = mock<Context>()
    private val crashTransformer = mock<ICrashTransformer>()
    private val config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
        .withCrashTransformer(crashTransformer)
        .build()

    @get:Rule
    val mainOrCrashReporterProviderMockedRule = MockedConstructionRule(MainOrCrashReporterProvider::class.java)
    @get:Rule
    val reporterBasedCrashProcessorMockedRule = MockedConstructionRule(ReporterBasedCrashProcessor::class.java)

    @Test
    fun createCrashProcessor() {
        val processor = ApplicationCrashProcessorCreator()
            .createCrashProcessor(context, config, reporterFactoryProvider) as ReporterBasedCrashProcessor

        val softly = SoftAssertions()
        softly.assertThat(reporterBasedCrashProcessorMockedRule.constructionMock.constructed()).hasSize(1)
        softly.assertThat(reporterBasedCrashProcessorMockedRule.argumentInterceptor.flatArguments()).hasSize(4)
        softly.assertThat(reporterBasedCrashProcessorMockedRule.argumentInterceptor.flatArguments()[0])
            .isSameAs(context)
        softly.assertThat(reporterBasedCrashProcessorMockedRule.argumentInterceptor.flatArguments()[1])
            .isSameAs(mainOrCrashReporterProviderMockedRule.constructionMock.constructed().first())
        softly.checkAppCrashRule(reporterBasedCrashProcessorMockedRule.argumentInterceptor.flatArguments()[2] as CrashProcessor.Rule)
        softly.assertThat(reporterBasedCrashProcessorMockedRule.argumentInterceptor.flatArguments()[3])
            .isSameAs(crashTransformer)

        softly.assertThat(mainOrCrashReporterProviderMockedRule.argumentInterceptor.flatArguments()).containsExactly(
            reporterFactoryProvider, config
        )
        softly.assertAll()
    }

    private fun SoftAssertions.checkAppCrashRule(rule: CrashProcessor.Rule) {
        assertThat(rule.shouldReportCrash(mock())).isTrue
    }
}
