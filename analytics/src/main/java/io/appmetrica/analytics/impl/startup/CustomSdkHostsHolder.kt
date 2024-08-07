package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class CustomSdkHostsHolder {

    private val tag = "[CustomSdkHostsHolder]"

    private val startupParamItemStatusAdapter = StartupParamItemAdapter()
    private var listMap: Map<String, List<String>> = emptyMap()
    var resultMap: Map<String, IdentifiersResult> = emptyMap()
        private set
    var commonResult: IdentifiersResult? = null
        private set

    @Synchronized
    fun update(result: IdentifiersResult) {
        DebugLogger.info(tag, "Update with $result")
        if (commonResult?.id.isNullOrEmpty() || !result.id.isNullOrEmpty()) {
            commonResult = result
            resultMap = JsonHelper.customSdkHostsFromString(result.id)?.mapValues {
                IdentifiersResult(
                    JsonHelper.listToJson(it.value).toString(),
                    result.status,
                    result.errorExplanation
                )
            } ?: emptyMap()
            listMap = resultMap.mapValues { JsonHelper.toStringList(it.value.id) ?: emptyList() }
        }
    }

    @Synchronized
    fun putToMap(identifiers: List<String>, map: MutableMap<String, StartupParamsItem>) {
        val mapWithRequestedIdentifiers = mutableMapOf<String, List<String>>()
        identifiers.forEach { identifier ->
            with(listMap[identifier]) {
                if (!isNullOrEmpty()) {
                    mapWithRequestedIdentifiers[identifier] = this
                }
            }
        }
        map[Constants.StartupParamsCallbackKeys.CUSTOM_SDK_HOSTS] =
            startupParamItemStatusAdapter.adapt(
                IdentifiersResult(
                    JsonHelper.customSdkHostsToString(mapWithRequestedIdentifiers),
                    commonResult?.status ?: IdentifierStatus.UNKNOWN,
                    commonResult?.errorExplanation
                )
            )
        DebugLogger.info(tag, "Fill map with identifiers: $identifiers, result: $mapWithRequestedIdentifiers")
    }
}
