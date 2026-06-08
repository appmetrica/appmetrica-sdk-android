package io.appmetrica.analytics.impl.core

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AppMetricaServiceCoreImplFirstCreateTaskProviderTest : CommonTest() {

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

    private fun reportKotlinVersionTask(): ReportKotlinVersionTask =
        reportKotlinVersionTaskMockedConstructionRule.singleWithArgs()
}
