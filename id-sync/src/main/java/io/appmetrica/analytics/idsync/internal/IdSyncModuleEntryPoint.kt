package io.appmetrica.analytics.idsync.internal

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.idsync.impl.IdSyncConstants
import io.appmetrica.analytics.idsync.impl.IdSyncController
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigParser
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigToProtoBytesConverter
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigToProtoConverter
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigWrapperJsonParser
import io.appmetrica.analytics.idsync.impl.model.IdSyncConfigWrapperProtobufConverter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class IdSyncModuleEntryPoint :
    ModuleServiceEntryPoint<IdSyncConfigWrapper>(), RemoteConfigUpdateListener<IdSyncConfigWrapper> {

    private val tag = "[IdSyncModuleEntryPoint]"

    private var controller: IdSyncController? = null

    private val configProtoConverter = IdSyncConfigToProtoConverter()
    private val configToBytesConverter = IdSyncConfigToProtoBytesConverter(configProtoConverter)
    private val configParser = IdSyncConfigParser(configProtoConverter)
    private val wrapperJsonParser = IdSyncConfigWrapperJsonParser(configParser)
    private val wrapperProtobufConverter = IdSyncConfigWrapperProtobufConverter(configToBytesConverter)

    override val identifier: String = IdSyncConstants.IDENTIFIER

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<IdSyncConfigWrapper>() {
            override fun getFeatures(): List<String> = listOf(IdSyncConstants.FEATURE_PARAMETER)

            override fun getBlocks(): Map<String, Int> = mapOf(IdSyncConstants.FEATURE_PARAMETER to 1)

            override fun getJsonParser(): JsonParser<IdSyncConfigWrapper> = wrapperJsonParser

            override fun getProtobufConverter(): Converter<IdSyncConfigWrapper, ByteArray> =
                wrapperProtobufConverter

            override fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<IdSyncConfigWrapper> =
                this@IdSyncModuleEntryPoint
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<IdSyncConfigWrapper?>,
    ) {
        DebugLogger.info(tag, "Init service side with config: $initialConfig")
        synchronized(this) {
            if (controller == null) {
                val controller = IdSyncController(serviceContext, initialConfig.identifiers)
                this.controller = controller
                initialConfig.featuresConfig?.config?.let { controller.refresh(it, initialConfig.identifiers) }
            }
        }
    }

    @Synchronized
    override fun onRemoteConfigUpdated(config: ModuleRemoteConfig<IdSyncConfigWrapper?>) {
        DebugLogger.info(tag, "Remote config updated: $config")
        config.featuresConfig?.config?.let { controller?.refresh(it, config.identifiers) }
    }
}
