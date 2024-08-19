package io.appmetrica.analytics.impl.clids

import android.content.ContentValues
import io.appmetrica.analytics.impl.ContentProviderDataParser
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.StartupUtils

private const val KEY_CLIDS = "clids"

internal class ClidsDataParser : ContentProviderDataParser<Map<String, String>> {

    override fun invoke(values: ContentValues): Map<String, String>? {
        val rawData = values.getAsString(KEY_CLIDS)
        val parsedData = JsonHelper.jsonToMap(rawData)
        return if (StartupUtils.isValidClids(parsedData)) {
            parsedData
        } else {
            SdkUtils.logAttribution("Passed clids ($rawData) are invalid.")
            null
        }
    }
}
