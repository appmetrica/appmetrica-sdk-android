package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.idsync.internal.IdSyncConfigWrapper
import io.appmetrica.analytics.idsync.internal.IdSyncConfigWrapper.Companion.toWrapper
import org.json.JSONObject

internal class IdSyncConfigWrapperJsonParser(
    private val parser: IdSyncConfigParser,
) : JsonParser<IdSyncConfigWrapper> {

    override fun parse(rawData: JSONObject): IdSyncConfigWrapper = parser.parse(rawData).toWrapper()

    override fun parseOrNull(rawData: JSONObject): IdSyncConfigWrapper? = parser.parseOrNull(rawData)?.toWrapper()
}
