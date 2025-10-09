package io.appmetrica.analytics.billing.internal

import io.appmetrica.analytics.billing.impl.BillingMonitorWrapper
import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.billing.impl.config.remote.RemoteBillingConfigConverter
import io.appmetrica.analytics.billing.impl.config.remote.RemoteBillingConfigParser
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class BillingServiceModuleEntryPoint : ModuleServiceEntryPoint<RemoteBillingConfig>() {

    private val tag = "[BillingServiceModuleEntryPoint]"

    private var billingMonitorWrapper: BillingMonitorWrapper? = null

    private val configParser = RemoteBillingConfigParser()
    private val configConverter = RemoteBillingConfigConverter()
    private val configUpdateListener = object : RemoteConfigUpdateListener<RemoteBillingConfig> {
        override fun onRemoteConfigUpdated(config: ModuleRemoteConfig<RemoteBillingConfig?>) {
            DebugLogger.info(tag, "Received config " + config.featuresConfig)
            billingMonitorWrapper?.updateConfig(
                config.featuresConfig?.let {
                    ServiceSideRemoteBillingConfig(it)
                }
            )
        }
    }

    override val identifier = Constants.MODULE_NAME

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<RemoteBillingConfig>() {
            override fun getFeatures() = emptyList<String>()

            override fun getBlocks() = mapOf(
                Constants.RemoteConfig.BLOCK_NAME_OBFUSCATED to Constants.RemoteConfig.BLOCK_VERSION
            )

            override fun getJsonParser(): JsonParser<RemoteBillingConfig> = configParser

            override fun getProtobufConverter(): Converter<RemoteBillingConfig, ByteArray> = configConverter

            override fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<RemoteBillingConfig> =
                configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<RemoteBillingConfig?>
    ) {
        DebugLogger.info(tag, "Init module with config ${initialConfig.featuresConfig}")

        billingMonitorWrapper = BillingMonitorWrapper(
            serviceContext,
            initialConfig.featuresConfig?.let { ServiceSideRemoteBillingConfig(it) }
        ).also {
            serviceContext.serviceModuleReporterComponentLifecycle.subscribe(it)
        }
    }
}
