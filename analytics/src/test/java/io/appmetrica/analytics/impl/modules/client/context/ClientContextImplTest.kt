package io.appmetrica.analytics.impl.modules.client.context

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.modules.client.ClientStorageProviderImpl
import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.impl.proxy.InternalClientModuleProxy
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ClientContextImplTest : CommonTest() {

    private val context: Context = mock()

    private val clientContext by setUp {
        ClientContextImpl(context)
    }

    @get:Rule
    val compositeModuleAdRevenueProcessorRule = constructionRule<CompositeModuleAdRevenueProcessor>()
    private val compositeModuleAdRevenueProcessor by compositeModuleAdRevenueProcessorRule

    @get:Rule
    val coreModuleAdRevenueContextRule = constructionRule<CoreModuleAdRevenueContextImpl>()
    private val coreModuleAdRevenueContext by coreModuleAdRevenueContextRule

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val clientStorageProviderRule = constructionRule<ClientStorageProviderImpl>()
    private val clientStorageProvider by clientStorageProviderRule

    @get:Rule
    val internalClientModuleFacadeRule = constructionRule<InternalClientModuleProxy>()
    private val internalClientModuleFacade by internalClientModuleFacadeRule

    @get:Rule
    val clientExecutorProviderRule = constructionRule<ModuleClientExecutorProviderImpl>()
    private val clientExecutorProvider by clientExecutorProviderRule

    @Test
    fun context() {
        assertThat(clientContext.context).isEqualTo(context)
    }

    @Test
    fun moduleAdRevenueContext() {
        assertThat(clientContext.moduleAdRevenueContext).isEqualTo(coreModuleAdRevenueContext)
        assertThat(coreModuleAdRevenueContextRule.argumentInterceptor.flatArguments())
            .containsExactly(compositeModuleAdRevenueProcessor)
    }

    @Test
    fun clientStorageProvider() {
        assertThat(clientContext.clientStorageProvider).isEqualTo(clientStorageProvider)
        assertThat(clientStorageProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context))
    }

    @Test
    fun internalClientModuleFacade() {
        assertThat(clientContext.internalClientModuleFacade).isEqualTo(internalClientModuleFacade)
    }

    @Test
    fun clientActivator() {
        clientContext.clientActivator.activate(context)
        verify(ClientServiceLocator.getInstance().anonymousClientActivator).activate(context)
    }

    @Test
    fun clientExecutorProvider() {
        assertThat(clientContext.clientExecutorProvider).isEqualTo(clientExecutorProvider)
    }
}
