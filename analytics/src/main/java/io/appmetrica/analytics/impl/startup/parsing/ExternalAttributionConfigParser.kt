package io.appmetrica.analytics.impl.startup.parsing

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.analytics.impl.db.state.converter.ExternalAttributionConfigConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import org.json.JSONObject

class ExternalAttributionConfigParser @VisibleForTesting constructor(
    private val externalAttributionConfigConverter: ExternalAttributionConfigConverter
) {

    constructor() : this(ExternalAttributionConfigConverter())

    fun parse(result: StartupResult, rootJson: JSONObject) {
        result.externalAttributionConfig =
            externalAttributionConfigConverter.toModel(parseExternalAttributionConfigToProto(rootJson))
    }

    private fun parseExternalAttributionConfigToProto(
        rootJson: JSONObject
    ): StartupStateProtobuf.StartupState.ExternalAttributionConfig {
        val proto = StartupStateProtobuf.StartupState.ExternalAttributionConfig()

        val jsonConfig = rootJson.optJSONObject(JsonResponseKey.EXTERNAL_ATTRIBUTION)

        if (jsonConfig != null) {
            proto.collectingInterval = RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(
                jsonConfig,
                JsonResponseKey.COLLECTING_INTERVAL_SECONDS,
                proto.collectingInterval
            )
        }

        return proto
    }
}
