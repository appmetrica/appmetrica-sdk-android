package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.MainReporterComponentId
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SelfDiagnosticMainClientUnitFactoryTest : CommonTest() {

    private val context: Context = mock()

    private val regularDispatcherComponent: RegularDispatcherComponent<*> = mock()
    private val repository: ComponentsRepository = mock {
        on { getRegularComponentIfExists(any()) } doReturn regularDispatcherComponent
    }

    private val packageName = "package name"
    private val apiKey = "api key"
    private val clientDescription: ClientDescription = mock {
        on { packageName } doReturn packageName
        on { apiKey } doReturn apiKey
    }
    private val sdkConfig: CommonArguments = mock()

    @get:Rule
    val selfDiagnosticClientUnitRule = constructionRule<SelfDiagnosticClientUnit>()

    @get:Rule
    val componentIdRule = constructionRule<MainReporterComponentId>()

    private val factory by setUp { SelfDiagnosticMainClientUnitFactory() }

    @Test
    fun createClientUnit() {
        assertThat(factory.createClientUnit(context, repository, clientDescription, sdkConfig))
            .isEqualTo(selfDiagnosticClientUnitRule.constructionMock.constructed().first())
        assertThat(selfDiagnosticClientUnitRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfDiagnosticClientUnitRule.argumentInterceptor.flatArguments())
            .containsExactly(regularDispatcherComponent)

        verify(repository).getRegularComponentIfExists(componentIdRule.constructionMock.constructed().first())
        assertThat(componentIdRule.constructionMock.constructed()).hasSize(1)
        assertThat(componentIdRule.argumentInterceptor.flatArguments())
            .containsExactly(packageName, apiKey)
    }
}
