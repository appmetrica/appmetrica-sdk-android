package io.appmetrica.analytics.coreapi.internal.data

import org.json.JSONObject

interface JsonParser<out T : Any> : Parser<JSONObject, T>
