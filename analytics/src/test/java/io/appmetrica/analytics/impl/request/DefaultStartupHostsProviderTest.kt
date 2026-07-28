package io.appmetrica.analytics.impl.request

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class DefaultStartupHostsProviderTest : CommonTest() {

    private val provider = DefaultStartupHostsProvider()
    private val defaultPredefinedHosts = arrayOf(
        "https://startup.mobile.yandex.net/"
    )

    @Test
    fun getHosts() {
        assertThat(provider.getHosts()).containsExactly(*defaultPredefinedHosts)
    }
}
