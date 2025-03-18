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
import io.appmetrica.analytics.screenshot.impl.ServiceToBundleScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.RemoteScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.RemoteScreenshotConfigParser
import io.appmetrica.analytics.screenshot.impl.config.remote.model.RemoteScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideRemoteScreenshotConfig

class ScreenshotServiceModuleEntryPoint : ModuleServiceEntryPoint<RemoteScreenshotConfig>() {

    private val tag = "[ScreenshotServiceModuleEntryPoint]"

    private var config: ServiceSideRemoteScreenshotConfig? = null

    private val bundleConverter = ServiceToBundleScreenshotConfigConverter()
    private val configParser = RemoteScreenshotConfigParser()
    private val configConverter = RemoteScreenshotConfigConverter()
    private val configUpdateListener = object : RemoteConfigUpdateListener<RemoteScreenshotConfig> {
        override fun onRemoteConfigUpdated(config: ModuleRemoteConfig<RemoteScreenshotConfig?>) {
            DebugLogger.info(tag, "Received config " + config.featuresConfig)
            this@ScreenshotServiceModuleEntryPoint.config = config.featuresConfig?.let {
                ServiceSideRemoteScreenshotConfig(it)
            }
        }
    }

    override val identifier = Constants.MODULE_ID

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<RemoteScreenshotConfig>() {
            override fun getFeatures() = listOf(
                Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED
            )

            override fun getBlocks() = mapOf(
                Constants.RemoteConfig.BLOCK_NAME_OBFUSCATED to Constants.RemoteConfig.BLOCK_VERSION
            )

            override fun getJsonParser(): JsonParser<RemoteScreenshotConfig> = configParser

            override fun getProtobufConverter(): Converter<RemoteScreenshotConfig, ByteArray> = configConverter

            override fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<RemoteScreenshotConfig> =
                configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<RemoteScreenshotConfig?>
    ) {
        DebugLogger.info(tag, "Init Screenshot")
        config = initialConfig.featuresConfig?.let { ServiceSideRemoteScreenshotConfig(it) }
        DebugLogger.info(tag, "Config is $config")
    }

    override val clientConfigProvider = object : ClientConfigProvider {
        override fun getConfigBundleForClient(): Bundle? {
            DebugLogger.info(tag, "Converting config '$config' to bundle")
            return bundleConverter.convert(config)
        }
    }
}
