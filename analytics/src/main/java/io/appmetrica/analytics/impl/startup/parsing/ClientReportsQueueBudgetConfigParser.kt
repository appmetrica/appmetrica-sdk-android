package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.impl.startup.ClientReportsQueueBudgetConfig
import org.json.JSONObject

internal class ClientReportsQueueBudgetConfigParser {

    fun parse(result: StartupResult, response: JSONObject) {
        result.clientReportsQueueBudgetConfig = parseClientReportsQueueBudgetConfig(response)
    }

    private fun parseClientReportsQueueBudgetConfig(
        response: JSONObject
    ): ClientReportsQueueBudgetConfig? {
        return ClientReportsQueueBudgetConfig.fromJson(
            response.optJSONObject(JsonResponseKey.CLIENT_REPORTS_QUEUE)
        )
    }
}
