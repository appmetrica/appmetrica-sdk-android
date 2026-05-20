package io.appmetrica.analytics.remotepermissions.impl.config.service

import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.remotepermissions.impl.config.service.converter.RemotePermissionsConfigProtoConverter
import io.appmetrica.analytics.remotepermissions.impl.config.service.parser.RemotePermissionsConfigJsonParser
import io.appmetrica.analytics.remotepermissions.internal.ServiceSideRemotePermissionsConfigWrapper
import io.appmetrica.analytics.remotepermissions.internal.ServiceSideRemotePermissionsConfigWrapper.Companion.toWrapper
import org.json.JSONObject

internal class ServiceSideRemotePermissionsConfigParser(
    private val protoConverter: RemotePermissionsConfigProtoConverter = RemotePermissionsConfigProtoConverter(),
    private val jsonParser: RemotePermissionsConfigJsonParser = RemotePermissionsConfigJsonParser()
) : JsonParser<ServiceSideRemotePermissionsConfigWrapper> {

    private val tag = "[ServiceSideRemotePermissionsConfigParser]"

    override fun parse(rawData: JSONObject): ServiceSideRemotePermissionsConfigWrapper {
        val config = protoConverter.toModel(jsonParser.parse(rawData))
        DebugLogger.info(tag, "Remote module config is '$config'")
        return config.toWrapper()
    }
}
