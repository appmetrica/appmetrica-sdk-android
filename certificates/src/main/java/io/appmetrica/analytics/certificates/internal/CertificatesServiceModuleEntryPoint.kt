package io.appmetrica.analytics.certificates.internal

import io.appmetrica.analytics.certificates.impl.Constants
import io.appmetrica.analytics.certificates.impl.config.service.ServiceSideNetworkCertificatesConfigConverter
import io.appmetrica.analytics.certificates.impl.config.service.ServiceSideNetworkCertificatesConfigParser
import io.appmetrica.analytics.certificates.impl.tls.CertificatesRuntime
import io.appmetrica.analytics.certificates.internal.ServiceSideNetworkCertificatesConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class CertificatesServiceModuleEntryPoint :
    ModuleServiceEntryPoint<ServiceSideNetworkCertificatesConfigWrapper>(),
    SslSocketFactoryProvider {

    private val tag = "[CertificatesServiceModuleEntryPoint]"
    private val runtime = CertificatesRuntime()
    private val configParser = ServiceSideNetworkCertificatesConfigParser()
    private val configConverter = ServiceSideNetworkCertificatesConfigConverter()
    private val configUpdateListener =
        object : RemoteConfigUpdateListener<ServiceSideNetworkCertificatesConfigWrapper> {
            override fun onRemoteConfigUpdated(
                config: ModuleRemoteConfig<ServiceSideNetworkCertificatesConfigWrapper?>
            ) {
                DebugLogger.info(tag, "Received config " + config.featuresConfig)
                runtime.updateConfig(config.featuresConfig?.config)
            }
        }

    override val identifier = Constants.MODULE_ID

    override val sslSocketFactory: SSLSocketFactory?
        get() = runtime.sslSocketFactory

    override val trustManager: X509TrustManager?
        get() = runtime.trustManager

    override val remoteConfigExtensionConfiguration =
        object : RemoteConfigExtensionConfiguration<ServiceSideNetworkCertificatesConfigWrapper>() {
            override fun getFeatures() = listOf(
                Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED
            )

            override fun getBlocks() = emptyMap<String, Int>()

            override fun getJsonParser(): JsonParser<ServiceSideNetworkCertificatesConfigWrapper> = configParser

            override fun getProtobufConverter(): Converter<ServiceSideNetworkCertificatesConfigWrapper, ByteArray> =
                object : Converter<ServiceSideNetworkCertificatesConfigWrapper, ByteArray> {
                    override fun fromModel(value: ServiceSideNetworkCertificatesConfigWrapper): ByteArray =
                        configConverter.fromModel(value.config)

                    override fun toModel(value: ByteArray): ServiceSideNetworkCertificatesConfigWrapper =
                        configConverter.toModel(value).toWrapper()
                }

            override fun getRemoteConfigUpdateListener():
                RemoteConfigUpdateListener<ServiceSideNetworkCertificatesConfigWrapper> = configUpdateListener
        }

    override fun initServiceSide(
        serviceContext: ServiceContext,
        initialConfig: ModuleRemoteConfig<ServiceSideNetworkCertificatesConfigWrapper?>
    ) {
        DebugLogger.info(tag, "Init Certificates")
        runtime.attach(serviceContext, initialConfig.featuresConfig?.config)
    }
}
