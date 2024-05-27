package io.appmetrica.analytics.remotepermissions.internal

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.common.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.service.LocationServiceExtension
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory
import io.appmetrica.analytics.remotepermissions.impl.FeatureConfig
import io.appmetrica.analytics.remotepermissions.impl.FeatureConfigToProtoBytesConverter
import io.appmetrica.analytics.remotepermissions.impl.FeatureParser
import io.appmetrica.analytics.remotepermissions.impl.RemoteConfigPermissionStrategy

class RemotePermissionsModuleEntryPoint :
    ModuleServiceEntryPoint<FeatureConfig>,
    AskForPermissionStrategyModuleProvider,
    RemoteConfigUpdateListener<FeatureConfig> {

    private val tag = "[RemotePermissionsModuleEntryPoint]"

    private val parser: JsonParser<FeatureConfig> = FeatureParser()
    private val converter: Converter<FeatureConfig, ByteArray> = FeatureConfigToProtoBytesConverter()
    private val listener: RemoteConfigUpdateListener<FeatureConfig> = this

    private val remoteConfigPermissionStrategy: RemoteConfigPermissionStrategy = RemoteConfigPermissionStrategy()

    override val askForPermissionStrategy: PermissionStrategy
        get() = remoteConfigPermissionStrategy

    override val identifier: String = "rp"

    override val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<FeatureConfig> =
        object : RemoteConfigExtensionConfiguration<FeatureConfig> {

            override fun getFeatures(): List<String> = emptyList()

            override fun getBlocks(): Map<String, Int> = mapOf("permissions" to 1)

            override fun getJsonParser(): JsonParser<FeatureConfig> = parser

            override fun getProtobufConverter(): Converter<FeatureConfig, ByteArray> = converter

            override fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<FeatureConfig> = listener
        }

    override val moduleEventServiceHandlerFactory: ModuleEventServiceHandlerFactory? = null

    override val locationServiceExtension: LocationServiceExtension? = null

    override val moduleServicesDatabase: ModuleServicesDatabase? = null

    override fun onRemoteConfigUpdated(config: ModuleRemoteConfig<FeatureConfig?>) {
        DebugLogger.info(
            tag,
            "omRemoteConfigUpdated with permitted permissions = ${config.featuresConfig?.permittedPermissions}"
        )
        remoteConfigPermissionStrategy.updatePermissions(
            config.featuresConfig?.permittedPermissions ?: emptySet()
        )
    }

    override fun initServiceSide(serviceContext: ServiceContext, initialConfig: ModuleRemoteConfig<FeatureConfig?>) {
        DebugLogger.info(
            tag,
            "initServiceSide with initial permitted permissions = " +
                "${initialConfig.featuresConfig?.permittedPermissions}"
        )
        remoteConfigPermissionStrategy.updatePermissions(
            initialConfig.featuresConfig?.permittedPermissions ?: emptySet()
        )
    }
}
