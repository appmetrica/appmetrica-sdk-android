package io.appmetrica.analytics.networktasks.impl

import android.net.Uri

internal class ForcedHttpsUrlProvider(url: String?) {

    val url: String? = prepareUrl(url)

    private fun prepareUrl(url: String?): String? {
        if (!url.isNullOrEmpty()) {
            val uri = Uri.parse(url)
            if ("http" == uri.scheme) {
                return uri.buildUpon().scheme("https").build().toString()
            }
        }
        return url
    }
}
