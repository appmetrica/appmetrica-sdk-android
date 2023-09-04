package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppMetricaServiceCoreImplFirstCreateTaskLauncherProviderTest : CommonTest() {

    @get:Rule
    val coreImplFirstCreateTaskLauncherMockedConstructionRule =
        MockedConstructionRule(CoreImplFirstCreateTaskLauncher::class.java)

    private val tasks = listOf(mock<Runnable>(), mock<Runnable>())

    @get:Rule
    val coreImplFirstCreateTaskProviderMockedConstructionRule =
        MockedConstructionRule(CoreImplFirstCreateTaskProvider::class.java) { mock, _ ->
            whenever(mock.tasks()).thenReturn(tasks)
        }

    private lateinit var coreImplFirstCreateTaskLauncherProvider: CoreImplFirstCreateTaskLauncherProvider

    @Before
    fun setUp() {
        coreImplFirstCreateTaskLauncherProvider = CoreImplFirstCreateTaskLauncherProvider()
    }

    @Test
    fun getLauncher() {
        assertThat(coreImplFirstCreateTaskLauncherProvider.getLauncher())
            .isEqualTo(metricaCoreImplFirstCreateTaskLauncher())
        checkMetricaCoreImplFirstCreateTaskProvider()
    }

    private fun metricaCoreImplFirstCreateTaskLauncher(): CoreImplFirstCreateTaskLauncher {
        assertThat(coreImplFirstCreateTaskLauncherMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(coreImplFirstCreateTaskLauncherMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(tasks)
        return coreImplFirstCreateTaskLauncherMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun checkMetricaCoreImplFirstCreateTaskProvider() {
        assertThat(coreImplFirstCreateTaskProviderMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(coreImplFirstCreateTaskProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }
}
