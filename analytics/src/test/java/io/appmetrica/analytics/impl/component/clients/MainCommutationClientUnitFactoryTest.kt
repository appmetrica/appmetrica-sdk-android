package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.CommutationComponentId
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponentFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MainCommutationClientUnitFactoryTest : CommonTest() {

    private val context: Context = mock()
    private val commutationDispatcherComponent: CommutationDispatcherComponent = mock()

    private val componentsRepository: ComponentsRepository = mock {
        on { getOrCreateCommutationComponent(any(), any(), any()) } doReturn commutationDispatcherComponent
    }

    private val packageName = "package name"
    private val clientDescription: ClientDescription = mock {
        on { packageName } doReturn packageName
    }

    private val sdkConfig: CommonArguments = mock()

    @get:Rule
    val componentIdRule = constructionRule<CommutationComponentId>()
    private val componentId by componentIdRule

    @get:Rule
    val commutationDispatcherComponentFactoryRule = constructionRule<CommutationDispatcherComponentFactory>()
    private val commutationDispatcherComponentFactory by commutationDispatcherComponentFactoryRule

    @get:Rule
    val commutationClientUnitRule = constructionRule<CommutationClientUnit>()

    private val factory by setUp { MainCommutationClientUnitFactory() }

    @Test
    fun createClientUnit() {
        assertThat(factory.createClientUnit(context, componentsRepository, clientDescription, sdkConfig))
            .isEqualTo(commutationClientUnitRule.constructionMock.constructed().first())
        assertThat(commutationClientUnitRule.constructionMock.constructed()).hasSize(1)
        assertThat(commutationClientUnitRule.argumentInterceptor.flatArguments())
            .containsExactly(context, commutationDispatcherComponent, sdkConfig)

        verify(componentsRepository)
            .getOrCreateCommutationComponent(componentId, sdkConfig, commutationDispatcherComponentFactory)

        assertThat(componentIdRule.constructionMock.constructed()).hasSize(1)
        assertThat(componentIdRule.argumentInterceptor.flatArguments()).containsExactly(packageName)

        assertThat(commutationDispatcherComponentFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(commutationDispatcherComponentFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
