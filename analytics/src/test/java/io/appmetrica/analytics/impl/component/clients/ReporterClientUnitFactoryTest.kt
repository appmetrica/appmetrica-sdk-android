package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory
import io.appmetrica.analytics.impl.component.ReporterComponentUnit
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class ReporterClientUnitFactoryTest : CommonTest() {

    private val componentUnitFactory: ComponentUnitFactory<ReporterComponentUnit> = mock()
    private val context: Context = mock()

    private val packageName = "package name"
    private val apiKey = "api key"

    private val clientDescription: ClientDescription = mock {
        on { packageName } doReturn packageName
        on { apiKey } doReturn apiKey
    }

    private val sdkConfig: CommonArguments = mock()
    private val regularDispatcherComponent: RegularDispatcherComponent<*> = mock()

    private val repository: ComponentsRepository = mock {
        on { getOrCreateRegularComponent(any(), any(), any()) } doReturn regularDispatcherComponent
    }

    @get:Rule
    val componentIdRule = constructionRule<ComponentId>()
    private val componentId by componentIdRule

    @get:Rule
    val regularClientUnit = constructionRule<RegularClientUnit>()

    @get:Rule
    val regularDispatcherComponentFactoryRule = constructionRule<RegularDispatcherComponentFactory<*>>()
    private val regularDispatcherComponentFactory by regularDispatcherComponentFactoryRule

    private val factory by setUp { ReporterClientUnitFactory(componentUnitFactory) }

    @Test
    fun createClientUnit() {
        assertThat(factory.createClientUnit(context, repository, clientDescription, sdkConfig))
            .isEqualTo(regularClientUnit.constructionMock.constructed().first())
        assertThat(regularClientUnit.constructionMock.constructed()).hasSize(1)
        assertThat(regularClientUnit.argumentInterceptor.flatArguments())
            .containsExactly(context, regularDispatcherComponent)

        verify(repository).getOrCreateRegularComponent(componentId, sdkConfig, regularDispatcherComponentFactory)

        assertThat(componentIdRule.constructionMock.constructed()).hasSize(1)
        assertThat(componentIdRule.argumentInterceptor.flatArguments()).containsExactly(packageName, apiKey)

        assertThat(regularDispatcherComponentFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(regularDispatcherComponentFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(componentUnitFactory)
    }
}
