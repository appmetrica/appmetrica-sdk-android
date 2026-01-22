package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.impl.StringArrayResourceRetriever
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

internal class DefaultStartupHostsProviderPredefinedHostsTest : CommonTest() {

    private val resourceRetriever = mock<StringArrayResourceRetriever>()
    private val provider = DefaultStartupHostsProvider(resourceRetriever)
    private val defaultStartupHost = "https://startup.mobile.yandex.net/"

    @Test
    fun nullHostsFromResources() {
        stubbing(resourceRetriever) {
            on { resource } doReturn null
        }
        assertThat(provider.getDefaultHosts()).containsExactly(defaultStartupHost)
    }

    @Test
    fun emptyHostsFromResources() {
        stubbing(resourceRetriever) {
            on { resource } doReturn arrayOf()
        }
        assertThat(provider.getDefaultHosts()).containsExactly(defaultStartupHost)
    }

    @Test
    fun hasOnlyNullAndEmptyHosts() {
        stubbing(resourceRetriever) {
            on { resource } doReturn arrayOf(null, "")
        }
        assertThat(provider.getDefaultHosts()).containsExactly(defaultStartupHost)
    }
}
