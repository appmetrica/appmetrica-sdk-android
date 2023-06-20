package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.ContentProviderHelper
import io.appmetrica.analytics.impl.clids.ClidsDataParser
import io.appmetrica.analytics.impl.clids.ClidsDataSaver

internal object ContentProviderHelperFactory {

    @JvmStatic
    fun createPreloadInfoHelper(): ContentProviderHelper<PreloadInfoState> =
        ContentProviderHelper(
            PreloadInfoDataParser(),
            PreloadInfoDataSaver(),
            "preload info"
        )

    @JvmStatic
    fun createClidsInfoHelper(): ContentProviderHelper<Map<String, String>> = ContentProviderHelper(
        ClidsDataParser(),
        ClidsDataSaver(),
        "clids"
    )
}
