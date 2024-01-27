package io.appmetrica.analytics.impl

import android.content.res.Configuration
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils

class LocaleExtractor {

    fun extractLocales(configuration: Configuration): List<String> =
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)) {
            LocalesHelperForN.getLocales(configuration)
        } else {
            listOf(PhoneUtils.normalizedLocale(configuration.locale))
        }
}
