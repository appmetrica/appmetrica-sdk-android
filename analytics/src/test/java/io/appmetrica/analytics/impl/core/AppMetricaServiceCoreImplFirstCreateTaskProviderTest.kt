package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppMetricaServiceCoreImplFirstCreateTaskProviderTest : CommonTest() {

    @get:Rule
    val reportKotlinVersionTaskMockedConstructionRule = MockedConstructionRule(ReportKotlinVersionTask::class.java)

    private lateinit var coreImplFirstCreateTaskProvider: CoreImplFirstCreateTaskProvider

    @Before
    fun setUp() {
        coreImplFirstCreateTaskProvider = CoreImplFirstCreateTaskProvider()
    }

    @Test
    fun tasks() {
        assertThat(coreImplFirstCreateTaskProvider.tasks()).containsExactly(reportKotlinVersionTask())
    }

    private fun reportKotlinVersionTask(): ReportKotlinVersionTask {
        assertThat(reportKotlinVersionTaskMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reportKotlinVersionTaskMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return reportKotlinVersionTaskMockedConstructionRule.constructionMock.constructed().first()
    }
}
