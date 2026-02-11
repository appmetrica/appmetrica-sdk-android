package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentEventTriggerProviderCreator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ReporterComponentUnit
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.RegularExecutorFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ReporterComponentUnitFactoryTest : CommonTest() {

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
    val regularExecutorFactoryRule = constructionRule<RegularExecutorFactory>()
    private val regularExecutorFactory by regularExecutorFactoryRule

    @get:Rule
    val reporterComponentUnitRule = constructionRule<ReporterComponentUnit>()

    @get:Rule
    val componentEventTriggerProviderCreatorRule = constructionRule<ComponentEventTriggerProviderCreator>()
    private val componentEventTriggerProviderCreator by componentEventTriggerProviderCreatorRule

    private val factory by setUp { ReporterComponentUnitFactory() }

    @Test
    fun createComponentUnit() {
        assertThat(factory.createComponentUnit(context, componentId, sdkConfig, startupUnit))
            .isEqualTo(reporterComponentUnitRule.constructionMock.constructed().first())
        assertThat(reporterComponentUnitRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterComponentUnitRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                componentId,
                sdkConfig,
                GlobalServiceLocator.getInstance().dataSendingRestrictionController,
                startupState,
                regularExecutorFactory,
                componentEventTriggerProviderCreator
            )

        assertThat(regularExecutorFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(regularExecutorFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(startupUnit)
    }
}
