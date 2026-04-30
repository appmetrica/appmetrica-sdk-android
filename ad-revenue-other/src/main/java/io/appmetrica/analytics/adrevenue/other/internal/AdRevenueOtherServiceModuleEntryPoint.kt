package io.appmetrica.analytics.adrevenue.other.internal

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.service.ServiceSideAdRevenueOtherConfigConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.service.ServiceSideAdRevenueOtherConfigParser
import io.appmetrica.analytics.adrevenue.other.impl.config.service.ServiceSideAdRevenueOtherConfigToBundleConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.internal.ServiceSideAdRevenueOtherConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ClientConfigProvider
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class AdRevenueOtherServiceModuleEntryPoint :
    ModuleServiceEntryPoint<ServiceSideAdRevenueOtherConfigWrapper>() {

    private val tag = "[AdRevenueOtherServiceModuleEntryPoint]"

    private var config: ServiceSideAdRevenueOtherConfig? = null

    private val bundleConverter = ServiceSideAdRevenueOtherConfigToBundleConverter()
    private val configParser = ServiceSideAdRevenueOtherConfigParser()
    private val configConverter = ServiceSideAdRevenueOtherConfigConverter()
    private val configUpdateListener =
        object : RemoteConfigUpdateListener<ServiceSideAdRevenueOtherConfigWrapper> {
            override fun onRemoteConfigUpdated(
                config: ModuleRemoteConfig<ServiceSideAdRevenueOtherConfigWrapper?>
            ) {
                DebugLogger.info(tag, "Received config " + config.featuresConfig)
                this@AdRevenueOtherServiceModuleEntryPoint.config = config.featuresConfig?.config
            }
        }

    override val identifier = Constants.MODULE_ID

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<ServiceSideAdRevenueOtherConfigWrapper>() {
            override fun getFeatures() = listOf(
                Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED,
                Constants.RemoteConfig.INCLUDE_SOURCE_NAME_OBFUSCATED,
            )

            override fun getBlocks() = emptyMap<String, Int>()

            override fun getJsonParser():
                JsonParser<ServiceSideAdRevenueOtherConfigWrapper> = configParser

            override fun getProtobufConverter():
                Converter<ServiceSideAdRevenueOtherConfigWrapper, ByteArray> =
                object : Converter<ServiceSideAdRevenueOtherConfigWrapper, ByteArray> {
                    override fun fromModel(value: ServiceSideAdRevenueOtherConfigWrapper): ByteArray =
                        configConverter.fromModel(value.config)

                    override fun toModel(value: ByteArray): ServiceSideAdRevenueOtherConfigWrapper =
                        configConverter.toModel(value).toWrapper()
                }

            override fun getRemoteConfigUpdateListener() = configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<ServiceSideAdRevenueOtherConfigWrapper?>
    ) {
        DebugLogger.info(tag, "Init AdRevenueOther")
        config = initialConfig.featuresConfig?.config
        DebugLogger.info(tag, "Config is $config")
    }

    override val clientConfigProvider = object : ClientConfigProvider {
        override fun getConfigBundleForClient(): Bundle? {
            DebugLogger.info(tag, "Converting config '$config' to bundle")
            return bundleConverter.convert(config)
        }
    }
}
