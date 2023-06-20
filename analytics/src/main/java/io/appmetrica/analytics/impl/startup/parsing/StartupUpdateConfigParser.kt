package io.appmetrica.analytics.impl.startup.parsing

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.impl.db.state.converter.StartupUpdateConfigConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.utils.JsonHelper
import org.json.JSONObject

internal class StartupUpdateConfigParser @VisibleForTesting constructor(
    private val converter: StartupUpdateConfigConverter
) {

    constructor() : this(StartupUpdateConfigConverter())

    fun parse(result: StartupResult, response: JSONObject) {
        val startupUpdateBlock = response.optJSONObject(JsonResponseKey.STARTUP_UPDATE)
        val startupUpdateProto = StartupStateProtobuf.StartupState.StartupUpdateConfig()
        JsonHelper.optIntegerOrNull(startupUpdateBlock, JsonResponseKey.STARTUP_UPDATE_INTERVAL_SECONDS)?.let {
            startupUpdateProto.interval = it
        }
        result.setStartupUpdateConfig(converter.toModel(startupUpdateProto))
    }
}
