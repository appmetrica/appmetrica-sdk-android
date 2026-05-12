package io.appmetrica.analytics.billing.internal

import io.appmetrica.analytics.billing.impl.BillingMonitorWrapper
import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.billing.impl.config.service.ServiceSideBillingConfigConverter
import io.appmetrica.analytics.billing.impl.config.service.ServiceSideBillingConfigParser
import io.appmetrica.analytics.billing.internal.ServiceSideBillingConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class BillingServiceModuleEntryPoint : ModuleServiceEntryPoint<ServiceSideBillingConfigWrapper>() {

    private val tag = "[BillingServiceModuleEntryPoint]"

    private var billingMonitorWrapper: BillingMonitorWrapper? = null

    private val configParser = ServiceSideBillingConfigParser()
    private val configConverter = ServiceSideBillingConfigConverter()
    private val configUpdateListener = object : RemoteConfigUpdateListener<ServiceSideBillingConfigWrapper> {
        override fun onRemoteConfigUpdated(config: ModuleRemoteConfig<ServiceSideBillingConfigWrapper?>) {
            DebugLogger.info(tag, "Received config " + config.featuresConfig)
            billingMonitorWrapper?.updateConfig(
                config.featuresConfig?.config
            )
        }
    }

    override val identifier = Constants.MODULE_NAME

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<ServiceSideBillingConfigWrapper>() {
            override fun getFeatures() = emptyList<String>()

            override fun getBlocks() = mapOf(
                Constants.RemoteConfig.BLOCK_NAME_OBFUSCATED to Constants.RemoteConfig.BLOCK_VERSION
            )

            override fun getJsonParser(): JsonParser<ServiceSideBillingConfigWrapper> = configParser

            override fun getProtobufConverter(): Converter<ServiceSideBillingConfigWrapper, ByteArray> =
                object : Converter<ServiceSideBillingConfigWrapper, ByteArray> {
                    override fun fromModel(value: ServiceSideBillingConfigWrapper): ByteArray =
                        configConverter.fromModel(value.config)

                    override fun toModel(value: ByteArray): ServiceSideBillingConfigWrapper =
                        configConverter.toModel(value).toWrapper()
                }

            override fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<ServiceSideBillingConfigWrapper> =
                configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<ServiceSideBillingConfigWrapper?>
    ) {
        DebugLogger.info(tag, "Init module with config ${initialConfig.featuresConfig}")

        billingMonitorWrapper = BillingMonitorWrapper(
            serviceContext,
            initialConfig.featuresConfig?.config
        ).also {
            serviceContext.serviceModuleReporterComponentLifecycle.subscribe(it)
        }
    }
}
