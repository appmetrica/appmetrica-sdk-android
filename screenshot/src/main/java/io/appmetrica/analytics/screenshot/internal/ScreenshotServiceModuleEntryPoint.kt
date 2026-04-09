package io.appmetrica.analytics.screenshot.internal

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ClientConfigProvider
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.service.ServiceSideScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.service.ServiceSideScreenshotConfigParser
import io.appmetrica.analytics.screenshot.impl.config.service.ServiceSideScreenshotConfigToBundleConverter
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.analytics.screenshot.internal.ServiceSideScreenshotConfigWrapper.Companion.toWrapper

class ScreenshotServiceModuleEntryPoint : ModuleServiceEntryPoint<ServiceSideScreenshotConfigWrapper>() {

    private val tag = "[ScreenshotServiceModuleEntryPoint]"

    private var config: ServiceSideScreenshotConfig? = null

    private val bundleConverter = ServiceSideScreenshotConfigToBundleConverter()
    private val configParser = ServiceSideScreenshotConfigParser()
    private val configConverter = ServiceSideScreenshotConfigConverter()
    private val configUpdateListener = object : RemoteConfigUpdateListener<ServiceSideScreenshotConfigWrapper> {
        override fun onRemoteConfigUpdated(config: ModuleRemoteConfig<ServiceSideScreenshotConfigWrapper?>) {
            DebugLogger.info(tag, "Received config " + config.featuresConfig)
            this@ScreenshotServiceModuleEntryPoint.config = config.featuresConfig?.config
        }
    }

    override val identifier = Constants.MODULE_ID

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<ServiceSideScreenshotConfigWrapper>() {
            override fun getFeatures() = listOf(
                Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED
            )

            override fun getBlocks() = mapOf(
                Constants.RemoteConfig.BLOCK_NAME_OBFUSCATED to Constants.RemoteConfig.BLOCK_VERSION
            )

            override fun getJsonParser(): JsonParser<ServiceSideScreenshotConfigWrapper> = configParser

            override fun getProtobufConverter(): Converter<ServiceSideScreenshotConfigWrapper, ByteArray> =
                object : Converter<ServiceSideScreenshotConfigWrapper, ByteArray> {
                    override fun fromModel(value: ServiceSideScreenshotConfigWrapper): ByteArray =
                        configConverter.fromModel(value.config)

                    override fun toModel(value: ByteArray): ServiceSideScreenshotConfigWrapper =
                        configConverter.toModel(value).toWrapper()
                }

            override fun getRemoteConfigUpdateListener():
                RemoteConfigUpdateListener<ServiceSideScreenshotConfigWrapper> = configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<ServiceSideScreenshotConfigWrapper?>
    ) {
        DebugLogger.info(tag, "Init Screenshot")
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
