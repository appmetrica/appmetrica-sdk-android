package io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.ServiceApplovinConfigConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.ServiceApplovinConfigParser
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.ServiceApplovinConfigToBundleConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ClientConfigProvider
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class AppLovinAutoServiceModuleEntryPoint :
    ModuleServiceEntryPoint<ServiceApplovinConfigWrapper>() {

    private val tag = "[AppLovinAutoServiceModuleEntryPoint]"

    private var config: ServiceApplovinConfig? = null

    private val bundleConverter = ServiceApplovinConfigToBundleConverter()
    private val configParser = ServiceApplovinConfigParser()
    private val configConverter = ServiceApplovinConfigConverter()
    private val configUpdateListener =
        object : RemoteConfigUpdateListener<ServiceApplovinConfigWrapper> {
            override fun onRemoteConfigUpdated(
                config: ModuleRemoteConfig<ServiceApplovinConfigWrapper?>
            ) {
                DebugLogger.info(tag, "Received config " + config.featuresConfig)
                this@AppLovinAutoServiceModuleEntryPoint.config = config.featuresConfig?.config
            }
        }

    override val identifier = Constants.MODULE_ID

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<ServiceApplovinConfigWrapper>() {
            override fun getFeatures() = listOf(
                Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED,
            )

            override fun getBlocks() = emptyMap<String, Int>()

            override fun getJsonParser():
                JsonParser<ServiceApplovinConfigWrapper> = configParser

            override fun getProtobufConverter():
                Converter<ServiceApplovinConfigWrapper, ByteArray> = configConverter

            override fun getRemoteConfigUpdateListener() = configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<ServiceApplovinConfigWrapper?>
    ) {
        config = initialConfig.featuresConfig?.config
        DebugLogger.info(tag, "initServiceSide: config=$config")
    }

    override val clientConfigProvider = object : ClientConfigProvider {
        override fun getConfigBundleForClient(): Bundle? {
            DebugLogger.info(tag, "Converting config '$config' to bundle")
            return bundleConverter.convert(config)
        }
    }
}
