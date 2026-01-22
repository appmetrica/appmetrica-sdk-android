package io.appmetrica.analytics.impl

import android.annotation.TargetApi
import android.content.res.Configuration
import android.os.Build

@TargetApi(Build.VERSION_CODES.N)
internal object LocalesHelperForN {

    @JvmStatic
    fun getLocales(configuration: Configuration): List<String> {
        val result = mutableListOf<String>()

        @Suppress("USELESS_ELVIS")
        val locales = configuration.locales ?: return result

        for (i in 0 until locales.size()) {
            locales[i]?.let { result.add(PhoneUtils.normalizedLocale(it)) }
        }
        return result
    }
}
