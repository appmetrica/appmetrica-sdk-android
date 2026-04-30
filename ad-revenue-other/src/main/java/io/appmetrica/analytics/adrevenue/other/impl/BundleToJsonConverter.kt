package io.appmetrica.analytics.adrevenue.other.impl

import android.os.Bundle
import org.json.JSONObject

internal object BundleToJsonConverter {

    fun convert(bundle: Bundle): String {
        val json = JSONObject()
        for (key in bundle.keySet()) {
            // Bundle.get(key) is deprecated in API 33+, but there's no type-safe alternative for unknown value types
            @Suppress("DEPRECATION")
            json.put(key, bundle.get(key)?.toString())
        }
        return json.toString()
    }
}
