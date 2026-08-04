package io.appmetrica.analytics.networkokhttp.impl

import android.util.LruCache
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.coreutils.internal.system.SystemPropertiesHelper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkokhttp.internal.InterceptorSupplier
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.security.KeyStore
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class OkHttpClientFactory {

    private val tag = "[OkHttpClientFactory]"
    private val debugInterceptorProperty = "debug.yndx.iaa.okhttp.mock"

    fun createOkHttpClient(settings: NetworkClientSettings): OkHttpClient {
        clients.get(settings)?.let { return it }
        synchronized(clients) {
            clients.get(settings)?.let { return it }
            return buildClient(settings).also { clients.put(settings, it) }
        }
    }

    private fun buildClient(settings: NetworkClientSettings): OkHttpClient {
        return baseClient.newBuilder().apply {
            protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            settings.readTimeout?.let { readTimeout(it.toLong(), MILLISECONDS) }
            settings.connectTimeout?.let { connectTimeout(it.toLong(), MILLISECONDS) }
            settings.callTimeout?.let { callTimeout(it, MILLISECONDS) }
            settings.instanceFollowRedirects?.let { followRedirects(it) }

            withDebugInterceptorSupplier()
            withSsl(settings.sslSocketFactory)
            withCaches(settings.useCaches)
        }.build()
    }

    private fun OkHttpClient.Builder.withSsl(sslSocketFactory: SSLSocketFactory?) {
        try {
            sslSocketFactory?.let { sslFactory ->
                DebugLogger.info(tag, "Setting custom sslFactory")
                val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                )
                trustManagerFactory.init(null as KeyStore?)
                val trustManager = trustManagerFactory.trustManagers
                    .filterIsInstance<X509TrustManager>()
                    .firstOrNull()
                if (trustManager != null) {
                    sslSocketFactory(sslFactory, trustManager)
                }
            }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
        }
    }

    private fun OkHttpClient.Builder.withCaches(useCaches: Boolean?) {
        useCaches?.let { useCaches ->
            if (!useCaches) {
                cache(null)
            } else {
                // do nothing
                // note: For HttpsURLConnection this flag helps to drop default requests cache
            }
        }
    }

    private fun OkHttpClient.Builder.withDebugInterceptorSupplier() {
        try {
            createDebugInterceptorSupplier()
                ?.get()
                ?.let { addInterceptor(it) }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
        }
    }

    // see https://nda.ya.ru/t/Aq0cfIc77NXYZt
    private fun createDebugInterceptorSupplier(): InterceptorSupplier? {
        val interceptorClassName = SystemPropertiesHelper.readSystemProperty(debugInterceptorProperty)
        if (interceptorClassName.isBlank()) {
            return null
        }

        val interceptor = ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            interceptorClassName,
            InterceptorSupplier::class.java
        )
        if (interceptor != null) {
            DebugLogger.info(tag, "Debug interceptor loaded: $interceptorClassName")
        } else {
            DebugLogger.warning(tag, "Failed to load debug interceptor: $interceptorClassName")
        }
        return interceptor
    }

    companion object {
        internal const val MAX_CACHED_CLIENTS = 20

        private val baseClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }

        private val clients = LruCache<NetworkClientSettings, OkHttpClient>(MAX_CACHED_CLIENTS)

        internal fun clearClientsCache() {
            clients.evictAll()
        }
    }
}
