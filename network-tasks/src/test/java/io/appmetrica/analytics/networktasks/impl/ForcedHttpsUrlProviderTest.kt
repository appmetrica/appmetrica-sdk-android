package io.appmetrica.analytics.networktasks.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ForcedHttpsUrlProviderTest {

    @Test
    fun testSecureHttpForcedToHttps() {
        val url = "http://secured.link/page?a=b"
        val provider = ForcedHttpsUrlProvider(url)
        assertThat(provider.url).isEqualTo("https://secured.link/page?a=b")
    }

    @Test
    fun invalidUrlIsNotChanged() {
        val url = "not a url"
        val provider = ForcedHttpsUrlProvider(url)
        assertThat(provider.url).isEqualTo(url)
    }

    @Test
    fun emptyUrlIsNotChanged() {
        val url = ""
        val provider = ForcedHttpsUrlProvider(url)
        assertThat(provider.url).isEqualTo(url)
    }

    @Test
    fun nullUrlIsNotChanged() {
        val provider = ForcedHttpsUrlProvider(null)
        assertThat(provider.url).isNull()
    }

    @Test
    fun testSecureHttpsLinkNotChanged() {
        val url = "https://very.secured.com/page?a=b"
        val provider = ForcedHttpsUrlProvider(url)
        assertThat(provider.url).isEqualTo("https://very.secured.com/page?a=b")
    }
}
