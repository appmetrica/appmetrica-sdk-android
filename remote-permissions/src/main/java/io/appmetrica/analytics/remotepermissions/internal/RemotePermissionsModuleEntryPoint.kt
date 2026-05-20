package io.appmetrica.analytics.remotepermissions.internal

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.common.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.remotepermissions.impl.Constants
import io.appmetrica.analytics.remotepermissions.impl.RemoteConfigPermissionStrategy
import io.appmetrica.analytics.remotepermissions.impl.config.service.ServiceSideRemotePermissionsConfigConverter
import io.appmetrica.analytics.remotepermissions.impl.config.service.ServiceSideRemotePermissionsConfigParser

class RemotePermissionsModuleEntryPoint :
    ModuleServiceEntryPoint<ServiceSideRemotePermissionsConfigWrapper>(),
    AskForPermissionStrategyModuleProvider,
    RemoteConfigUpdateListener<ServiceSideRemotePermissionsConfigWrapper> {

    private val tag = "[RemotePermissionsModuleEntryPoint]"

    private val parser = ServiceSideRemotePermissionsConfigParser()
    private val converter = ServiceSideRemotePermissionsConfigConverter()
    private val listener: RemoteConfigUpdateListener<ServiceSideRemotePermissionsConfigWrapper> = this

    private val remoteConfigPermissionStrategy: RemoteConfigPermissionStrategy =
        RemoteConfigPermissionStrategy()

    override val askForPermissionStrategy: PermissionStrategy
        get() = remoteConfigPermissionStrategy

    override val identifier: String = Constants.MODULE_NAME

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<ServiceSideRemotePermissionsConfigWrapper>() {

            override fun getFeatures(): List<String> = emptyList()

            override fun getBlocks(): Map<String, Int> =
                mapOf(Constants.RemoteConfig.BLOCK_NAME to 1)

            override fun getJsonParser():
                JsonParser<ServiceSideRemotePermissionsConfigWrapper> = parser

            override fun getProtobufConverter():
                Converter<ServiceSideRemotePermissionsConfigWrapper, ByteArray> = converter

            override fun getRemoteConfigUpdateListener():
                RemoteConfigUpdateListener<ServiceSideRemotePermissionsConfigWrapper> = listener
        }

    override fun onRemoteConfigUpdated(
        config: ModuleRemoteConfig<ServiceSideRemotePermissionsConfigWrapper?>
    ) {
        DebugLogger.info(
            tag,
            "omRemoteConfigUpdated with permitted permissions = " +
                "${config.featuresConfig?.config?.permittedPermissions}"
        )
        remoteConfigPermissionStrategy.updatePermissions(
            config.featuresConfig?.config?.permittedPermissions ?: emptySet()
        )
    }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<ServiceSideRemotePermissionsConfigWrapper?>
    ) {
        DebugLogger.info(
            tag,
            "initServiceSide with initial permitted permissions = " +
                "${initialConfig.featuresConfig?.config?.permittedPermissions}"
        )
        remoteConfigPermissionStrategy.updatePermissions(
            initialConfig.featuresConfig?.config?.permittedPermissions ?: emptySet()
        )
    }
}
