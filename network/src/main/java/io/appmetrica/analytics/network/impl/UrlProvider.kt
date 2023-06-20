package io.appmetrica.analytics.network.impl

import java.net.URL

internal class UrlProvider {

    fun createUrl(url: String?): URL = URL(url)
}
