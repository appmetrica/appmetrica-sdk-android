package io.appmetrica.analytics.certificates.impl.tls

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

internal class CertificatesRuntime(
    private val createTrustManager: (Context) -> X509TrustManager? =
        CertificatesTrustFactory::createSystemFirstModuleTrustManager,
    private val createSocketFactory: (X509TrustManager) -> SSLSocketFactory =
        CertificatesTrustFactory::createSocketFactory,
) {

    private val tag = "[CertificatesRuntime]"

    @Volatile
    private var serviceContext: ServiceContext? = null

    @Volatile
    var config: ServiceSideNetworkCertificatesConfig = ServiceSideNetworkCertificatesConfig()
        private set

    @Volatile
    var sslSocketFactory: SSLSocketFactory? = null
        private set

    @Volatile
    var trustManager: X509TrustManager? = null
        private set

    @Synchronized
    fun attach(serviceContext: ServiceContext, config: ServiceSideNetworkCertificatesConfig?) {
        val previousContext = this.serviceContext?.context
        this.serviceContext = serviceContext
        val newConfig = config ?: ServiceSideNetworkCertificatesConfig()
        val enabledUnchanged = newConfig.enabled == this.config.enabled
        val contextUnchanged = previousContext === serviceContext.context
        this.config = newConfig
        DebugLogger.info(tag, "attach: $newConfig")
        if (enabledUnchanged && contextUnchanged && isDesiredStateReached()) {
            DebugLogger.info(tag, "attach: skip rebuild, config unchanged")
            return
        }
        rebuild()
    }

    @Synchronized
    fun updateConfig(config: ServiceSideNetworkCertificatesConfig?) {
        val newConfig = config ?: ServiceSideNetworkCertificatesConfig()
        val enabledUnchanged = newConfig.enabled == this.config.enabled
        this.config = newConfig
        DebugLogger.info(tag, "updateConfig: ${this.config}")
        if (enabledUnchanged && isDesiredStateReached()) {
            DebugLogger.info(tag, "updateConfig: skip rebuild, config unchanged")
            return
        }
        rebuild()
    }

    @VisibleForTesting
    @Synchronized
    internal fun reset() {
        serviceContext = null
        config = ServiceSideNetworkCertificatesConfig()
        sslSocketFactory = null
        trustManager = null
    }

    private fun isDesiredStateReached(): Boolean {
        val context = serviceContext?.context
        return if (context != null && config.enabled) {
            sslSocketFactory != null && trustManager != null
        } else {
            sslSocketFactory == null && trustManager == null
        }
    }

    private fun rebuild() {
        val context = serviceContext?.context
        if (context == null || !config.enabled) {
            DebugLogger.info(tag, "rebuild: disable TLS override (enabled=${config.enabled})")
            sslSocketFactory = null
            trustManager = null
            return
        }
        try {
            val createdTrustManager = createTrustManager(context)
            if (createdTrustManager == null) {
                DebugLogger.info(tag, "rebuild: failed to create trust manager")
                sslSocketFactory = null
                trustManager = null
                return
            }
            val createdSocketFactory = createSocketFactory(createdTrustManager)
            trustManager = createdTrustManager
            sslSocketFactory = createdSocketFactory
            DebugLogger.info(tag, "rebuild: TLS ready")
        } catch (throwable: Throwable) {
            DebugLogger.error(tag, throwable, "rebuild: failed to create TLS pair")
            sslSocketFactory = null
            trustManager = null
        }
    }
}
