package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf.IdSyncConfig.Header
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import org.json.JSONArray
import org.json.JSONObject

internal class IdSyncConfigParser(
    private val converter: IdSyncConfigToProtoConverter
) : JsonParser<IdSyncConfig> {

    private val feature = "id_sync"
    private val block = "id_sync"
    private val launchDelaySeconds = "launch_delay_seconds"
    private val requests = "requests"
    private val type = "type"
    private val url = "url"
    private val headers = "headers"
    private val resendIntervalForValidResponse = "resend_interval_for_valid_response"
    private val resendIntervalForInvalidResponse = "resend_interval_for_invalid_response"
    private val validResponseCodes = "valid_response_codes"
    private val preconditions = "preconditions"
    private val network = "network"
    private val networkCell = "cell"

    override fun parse(rawData: JSONObject): IdSyncConfig {
        val blockJson = rawData.optJSONObject(block) ?: JSONObject()
        val result = IdSyncProtobuf.IdSyncConfig().apply {
            enabled = RemoteConfigJsonUtils.extractFeature(rawData, feature, enabled)
            requestConfig = IdSyncProtobuf.IdSyncConfig.RequestConfig().apply {
                launchDelay = RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(
                    blockJson,
                    launchDelaySeconds,
                    launchDelay
                )
                requests = blockJson.extractRequests()
            }
        }
        return converter.toModel(result)
    }

    private fun JSONObject.extractRequests(): Array<IdSyncProtobuf.IdSyncConfig.Request> {
        val requests = optJSONArray(requests) ?: return emptyArray()
        val result = Array(requests.length()) {
            requests.optJSONObject(it).toRequest()
        }
        return result
    }

    private fun JSONObject?.toRequest(): IdSyncProtobuf.IdSyncConfig.Request {
        val result = IdSyncProtobuf.IdSyncConfig.Request()
        this ?: return result

        result.type = optString(type).toByteArray()
        result.preconditions = optJSONObject(preconditions).toPreconditions()
        result.url = optString(url).toByteArray()
        result.headers = optJSONObject(headers).toHeaders()
        result.resendIntervalForValidResponse = RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(
            this,
            resendIntervalForValidResponse,
            result.resendIntervalForValidResponse
        )
        result.resendIntervalForInvalidResponse = RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(
            this,
            resendIntervalForInvalidResponse,
            result.resendIntervalForInvalidResponse
        )
        result.validResponseCodes = optJSONArray(validResponseCodes).extractValidResponseCodes()

        return result
    }

    private fun JSONObject?.toPreconditions(): IdSyncProtobuf.IdSyncConfig.Preconditions {
        val result = IdSyncProtobuf.IdSyncConfig.Preconditions()
        this ?: return result

        val networkType = optString(network)
        if (networkType == networkCell) {
            result.networkType = IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_CELL
        }
        return result
    }

    private fun JSONObject?.toHeaders(): Array<Header> {
        this ?: return emptyArray()

        val headers = mutableListOf<Header>()
        keys().forEach { jsonHeaderName ->
            headers.add(
                Header().apply {
                    name = jsonHeaderName.toByteArray()
                    value = optJSONArray(jsonHeaderName).toHeaderValues()
                }
            )
        }
        return headers.toTypedArray()
    }

    private fun JSONArray?.toHeaderValues(): Array<ByteArray> = if (this == null) {
        emptyArray()
    } else {
        Array(this.length()) {
            optString(it).toByteArray()
        }
    }

    private fun JSONArray?.extractValidResponseCodes(): IntArray {
        val result = if (this == null) {
            IntArray(0)
        } else {
            IntArray(this.length()) {
                optInt(it)
            }
        }
        return result.filter { it != 0 }.takeIf { it.isNotEmpty() }?.toIntArray() ?: IntArray(1) { 200 }
    }
}
