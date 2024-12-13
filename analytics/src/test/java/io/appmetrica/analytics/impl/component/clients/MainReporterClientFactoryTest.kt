package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.MainReporterComponentId
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class MainReporterClientFactoryTest : CommonTest() {

    private val context: Context = mock()

    private val packageName = "package name"
    private val apiKey = "api key"
    private val clientDescription: ClientDescription = mock {
        on { packageName } doReturn packageName
        on { apiKey } doReturn apiKey
    }

    private val sdkConfig: CommonArguments = mock()

    private val component: RegularDispatcherComponent<*> = mock()
    private val repository: ComponentsRepository = mock {
        on { getOrCreateRegularComponent(any(), any(), any()) } doReturn component
    }

    @get:Rule
    val componentIdRule = constructionRule<MainReporterComponentId>()
    private val componentId by componentIdRule

    @get:Rule
    val regularDispatcherComponentFactoryRule = constructionRule<RegularDispatcherComponentFactory<*>>()
    private val regularDispatcherComponentFactory by regularDispatcherComponentFactoryRule

    @get:Rule
    val mainReporterComponentFactoryRule = constructionRule<MainReporterComponentUnitFactory>()
    private val mainReporterComponentFactory by mainReporterComponentFactoryRule

    @get:Rule
    val mainReporterClientUnitRule = constructionRule<MainReporterClientUnit>()

    private val factory by setUp { MainReporterClientFactory() }

    @Test
    fun createClientUnit() {
        assertThat(factory.createClientUnit(context, repository, clientDescription, sdkConfig))
            .isEqualTo(mainReporterClientUnitRule.constructionMock.constructed().first())
        assertThat(mainReporterClientUnitRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterClientUnitRule.argumentInterceptor.flatArguments())
            .containsExactly(context, component)

        verify(repository).getOrCreateRegularComponent(componentId, sdkConfig, regularDispatcherComponentFactory)

        assertThat(componentIdRule.constructionMock.constructed()).hasSize(1)
        assertThat(componentIdRule.argumentInterceptor.flatArguments()).containsExactly(packageName, apiKey)

        assertThat(regularDispatcherComponentFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(regularDispatcherComponentFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporterComponentFactory)

        assertThat(mainReporterComponentFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterComponentFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
