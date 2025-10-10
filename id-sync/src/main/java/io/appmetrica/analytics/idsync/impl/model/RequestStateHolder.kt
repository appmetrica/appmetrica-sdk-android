package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences

internal class RequestStateHolder(private val modulePreferences: ModulePreferences) {

    private val tag = "[RequestStateHolder]"

    private val requestStateConverter = RequestStateConverter()
    private val requestStatePrefKey = "request_state"

    private val requestStates: MutableMap<String, RequestState> = loadRequestStates().toMutableMap()

    fun getRequestState(type: String): RequestState? = requestStates[type]

    fun updateRequestState(requestState: RequestState) {
        DebugLogger.info(tag, "Updating request state: $requestState")
        requestStates[requestState.type] = requestState
        saveRequestStates(requestStates)
    }

    private fun loadRequestStates(): Map<String, RequestState> =
        requestStateConverter.toModel(modulePreferences.getString(requestStatePrefKey, null))
            .associateBy { it.type }.also { DebugLogger.info(tag, "Loaded request states: $it") }

    private fun saveRequestStates(state: Map<String, RequestState>) {
        DebugLogger.info(tag, "Saving request states: $state")
        modulePreferences.putString(requestStatePrefKey, requestStateConverter.fromModel(state.values.toList()))
    }
}
