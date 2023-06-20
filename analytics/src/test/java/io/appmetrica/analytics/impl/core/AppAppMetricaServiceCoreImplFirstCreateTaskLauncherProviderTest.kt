package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppAppMetricaServiceCoreImplFirstCreateTaskLauncherProviderTest : CommonTest() {

    @get:Rule
    val metricaCoreImplFirstCreateTaskLauncherMockedConstructionRule =
        MockedConstructionRule(MetricaCoreImplFirstCreateTaskLauncher::class.java)

    private val tasks = listOf(mock<Runnable>(), mock<Runnable>())

    @get:Rule
    val metricaCoreImplFirstCreateTaskProviderMockedConstructionRule =
        MockedConstructionRule(MetricaCoreImplFirstCreateTaskProvider::class.java) {mock, _ ->
            whenever(mock.tasks()).thenReturn(tasks)
        }

    private lateinit var metricaCoreImplFirstCreateTaskLauncherProvider: MetricaCoreImplFirstCreateTaskLauncherProvider

    @Before
    fun setUp() {
        metricaCoreImplFirstCreateTaskLauncherProvider = MetricaCoreImplFirstCreateTaskLauncherProvider()
    }

    @Test
    fun getLauncher() {
        assertThat(metricaCoreImplFirstCreateTaskLauncherProvider.getLauncher())
            .isEqualTo(metricaCoreImplFirstCreateTaskLauncher())
        checkMetricaCoreImplFirstCreateTaskProvider()
    }

    private fun metricaCoreImplFirstCreateTaskLauncher(): MetricaCoreImplFirstCreateTaskLauncher {
        assertThat(metricaCoreImplFirstCreateTaskLauncherMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(metricaCoreImplFirstCreateTaskLauncherMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(tasks)
        return metricaCoreImplFirstCreateTaskLauncherMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun checkMetricaCoreImplFirstCreateTaskProvider() {
        assertThat(metricaCoreImplFirstCreateTaskProviderMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(metricaCoreImplFirstCreateTaskProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }
}
