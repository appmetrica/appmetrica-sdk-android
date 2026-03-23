package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage.EnvironmentVariable
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.telephony.SimInfo
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider
import io.appmetrica.analytics.impl.telephony.TelephonyInfoAdapter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class ReportMessageBuilder(
    private val telephonyDataProvider: TelephonyDataProvider,
) {

    private val tag = "[ReportMessageBuilder]"

    fun build(
        sessionData: ReportSessionData,
        dbRequestConfig: DbNetworkTaskConfig,
        requestConfig: ReportRequestConfig,
        certificates: List<String>,
    ): EventProto.ReportMessage {
        val reportMessage = EventProto.ReportMessage()

        val requestParameters = EventProto.ReportMessage.RequestParameters()
        requestParameters.uuid = WrapUtils.getOrDefaultIfEmpty(dbRequestConfig.uuid, requestConfig.uuid)
        requestParameters.deviceId = WrapUtils.getOrDefaultIfEmpty(dbRequestConfig.deviceId, requestConfig.deviceId)
        reportMessage.reportRequestParameters = requestParameters

        fillTelephonyProviderInfo(reportMessage)

        reportMessage.sessions = sessionData.sessions.toTypedArray()
        reportMessage.appEnvironment = extractEnvironment(sessionData.environment)
        reportMessage.certificatesSha1Fingerprints = certificates.toTypedArray()
        fillAdditionalApiKeys(reportMessage, requestConfig.autoCollectedDataSubscribers)

        return reportMessage
    }

    private fun fillTelephonyProviderInfo(reportMessage: EventProto.ReportMessage) {
        telephonyDataProvider.adoptSimInfo(object : TelephonyInfoAdapter<List<SimInfo>> {
            override fun adopt(value: List<SimInfo>) {
                if (!Utils.isNullOrEmpty(value)) {
                    reportMessage.simInfo = Array(value.size) { i ->
                        ProtobufUtils.buildSimInfo(value[i])
                    }
                }
            }
        })
    }

    private fun fillAdditionalApiKeys(
        reportMessage: EventProto.ReportMessage,
        autoCollectedDataSubscribers: Set<String>,
    ) {
        val additionalApiKeys = autoCollectedDataSubscribers.toTypedArray()
        reportMessage.additionalApiKeys = Array(additionalApiKeys.size) { i ->
            StringUtils.getUTF8Bytes(additionalApiKeys[i])
        }
    }

    internal fun extractEnvironment(data: JSONObject): Array<EnvironmentVariable>? {
        val envLength = data.length()
        if (envLength <= 0) return null

        val variables = ArrayList<EnvironmentVariable>(envLength)
        val keys = data.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                val variable = EnvironmentVariable()
                variable.name = key
                variable.value = data.getString(key)
                variables.add(variable)
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, "Can not find string value for key %s", key)
            }
        }
        return variables.toTypedArray()
    }
}
