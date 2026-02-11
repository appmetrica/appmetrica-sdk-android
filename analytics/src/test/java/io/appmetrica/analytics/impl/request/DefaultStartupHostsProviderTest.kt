package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StringArrayResourceRetriever
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ConstructionArgumentCaptor
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

internal class DefaultStartupHostsProviderTest : CommonTest() {

    private val resourceRetriever = mock<StringArrayResourceRetriever>()
    private val provider = DefaultStartupHostsProvider(resourceRetriever)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @Test
    fun defaultConstructor() {
        val captor = ConstructionArgumentCaptor<StringArrayResourceRetriever>()
        val newMock = Mockito.mockConstruction(StringArrayResourceRetriever::class.java, captor)
        try {
            DefaultStartupHostsProvider()
            assertThat(captor.flatArguments()).containsExactly(
                GlobalServiceLocator.getInstance().context,
                "appmetrica_startup_hosts"
            )
        } finally {
            newMock.close()
        }
    }

    @Test
    fun hasHostsFromResourcesNotFiltered() {
        val hosts = arrayOf<String?>("host1", "host2", "host3")
        stubbing(resourceRetriever) {
            on { resource } doReturn hosts
        }
        assertThat(provider.getDefaultHosts()).containsExactly(*hosts)
    }

    @Test
    fun hasHostsFromResourcesShouldFilter() {
        val hosts = arrayOf("host1", null, "host3", "", "host5", "   ", "host7")
        stubbing(resourceRetriever) {
            on { resource } doReturn hosts
        }
        assertThat(provider.getDefaultHosts()).containsExactly("host1", "host3", "host5", "host7")
    }
}
