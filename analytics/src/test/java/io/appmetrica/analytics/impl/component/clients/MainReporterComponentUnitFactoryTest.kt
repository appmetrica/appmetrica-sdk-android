package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.MainReporterComponentUnit
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.RegularExecutorFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainReporterComponentUnitFactoryTest : CommonTest() {

    private val context: Context = mock()
    private val componentId: ComponentId = mock()
    private val sdkConfig: CommonArguments.ReporterArguments = mock()
    private val startupState: StartupState = mock()

    private val startupUnit: StartupUnit = mock {
        on { startupState } doReturn startupState
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val mainReporterComponentUnitRule = constructionRule<MainReporterComponentUnit>()

    @get:Rule
    val regularExecutorFactoryRule = constructionRule<RegularExecutorFactory>()

    private val factory by setUp { MainReporterComponentUnitFactory() }

    @Test
    fun createComponentUnit() {
        assertThat(factory.createComponentUnit(context, componentId, sdkConfig, startupUnit))
            .isEqualTo(mainReporterComponentUnitRule.constructionMock.constructed().first())
        assertThat(mainReporterComponentUnitRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterComponentUnitRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                startupState,
                componentId,
                sdkConfig,
                GlobalServiceLocator.getInstance().referrerHolder,
                GlobalServiceLocator.getInstance().dataSendingRestrictionController,
                regularExecutorFactoryRule.constructionMock.constructed().first()
            )
        assertThat(regularExecutorFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(regularExecutorFactoryRule.argumentInterceptor.flatArguments()).containsExactly(startupUnit)
    }
}
