package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentEventTriggerProviderCreator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.SelfReportingArgumentsFactory
import io.appmetrica.analytics.impl.component.SelfSdkReportingComponentUnit
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.StubbedExecutorFactory
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
internal class SelfSdkReporterComponentUnitFactoryTest : CommonTest() {

    private val context: Context = mock()
    private val componentId: ComponentId = mock()
    private val sdkConfig: CommonArguments.ReporterArguments = mock()

    private val startupState: StartupState = mock()
    private val startupUnit: StartupUnit = mock {
        on { startupState } doReturn startupState
    }

    @get:Rule
    val selfSdkReporterComponentUnitRule = constructionRule<SelfSdkReportingComponentUnit>()

    @get:Rule
    val selfReportingArgumentsFactoryRule = constructionRule<SelfReportingArgumentsFactory>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val stubbedExecutorFactoryRule = constructionRule<StubbedExecutorFactory>()

    @get:Rule
    val componentEventTriggerProviderCreatorRule = constructionRule<ComponentEventTriggerProviderCreator>()
    private val componentEventTriggerProviderCreator by componentEventTriggerProviderCreatorRule

    private val factory by setUp { SelfSdkReporterComponentUnitFactory() }

    @Test
    fun createComponentUnit() {
        assertThat(factory.createComponentUnit(context, componentId, sdkConfig, startupUnit))
            .isEqualTo(selfSdkReporterComponentUnitRule.constructionMock.constructed().first())
        assertThat(selfSdkReporterComponentUnitRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfSdkReporterComponentUnitRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                startupState,
                componentId,
                sdkConfig,
                selfReportingArgumentsFactoryRule.constructionMock.constructed().first(),
                stubbedExecutorFactoryRule.constructionMock.constructed().first(),
                componentEventTriggerProviderCreator
            )

        assertThat(selfReportingArgumentsFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfReportingArgumentsFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(GlobalServiceLocator.getInstance().dataSendingRestrictionController)

        assertThat(stubbedExecutorFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(stubbedExecutorFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
