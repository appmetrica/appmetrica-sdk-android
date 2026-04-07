package io.appmetrica.analytics.networkapi

import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

/**
 * Configuration settings for [NetworkClient].
 * Use [Builder] to construct instances of this class.
 *
 * @property connectTimeout Connection timeout in milliseconds, or null to use default
 * @property readTimeout Read timeout in milliseconds, or null to use default
 * @property callTimeout Total call timeout in milliseconds (DNS + connect + write + read), or null to use default
 * @property sslSocketFactory Custom SSL socket factory for HTTPS connections, or null to use default
 * @property useCaches Whether to use caching for HTTP connections, or null to use default
 * @property instanceFollowRedirects Whether to follow HTTP redirects automatically, or null to use default
 * @property maxResponseSize Maximum size of response data in bytes
 * @property collectMetrics Whether to collect [NetworkCallMetrics] and attach them to [Response.metrics], or null to use default (no collection)
 */
class NetworkClientSettings private constructor(
    val connectTimeout: Int?,
    val readTimeout: Int?,
    val callTimeout: Long?,
    val sslSocketFactory: SSLSocketFactory?,
    val useCaches: Boolean?,
    val instanceFollowRedirects: Boolean?,
    val maxResponseSize: Int,
    val collectMetrics: Boolean?,
) {

    override fun toString(): String {
        return "NetworkClientSettings(" +
            "connectTimeout=$connectTimeout, " +
            "readTimeout=$readTimeout, " +
            "callTimeout=$callTimeout, " +
            "sslSocketFactory=$sslSocketFactory, " +
            "useCaches=$useCaches, " +
            "instanceFollowRedirects=$instanceFollowRedirects, " +
            "maxResponseSize=$maxResponseSize, " +
            "collectMetrics=$collectMetrics" +
            ")"
    }

    /**
     * Builder for creating [NetworkClientSettings] instances.
     * Provides a fluent API for configuring network client settings.
     */
    class Builder {

        private var connectTimeout: Int? = null
        private var readTimeout: Int? = null
        private var callTimeout: Long? = null
        private var sslSocketFactory: SSLSocketFactory? = null
        private var useCaches: Boolean? = null
        private var instanceFollowRedirects: Boolean? = null
        private var maxResponseSize: Int = Int.MAX_VALUE
        private var collectMetrics: Boolean? = null

        /**
         * Sets the connection timeout.
         *
         * @param connectTimeout Connection timeout in milliseconds
         * @return This builder instance for method chaining
         */
        fun withConnectTimeout(connectTimeout: Int): Builder {
            this.connectTimeout = connectTimeout
            return this
        }

        /**
         * Sets the read timeout.
         *
         * @param readTimeout Read timeout in milliseconds
         * @return This builder instance for method chaining
         */
        fun withReadTimeout(readTimeout: Int): Builder {
            this.readTimeout = readTimeout
            return this
        }

        /**
         * Sets the total call timeout (DNS + connect + write + read).
         *
         * @param callTimeout Total call timeout value
         * @param timeUnit Unit of the timeout value
         * @return This builder instance for method chaining
         */
        fun withCallTimeout(callTimeout: Long, timeUnit: TimeUnit): Builder {
            this.callTimeout = timeUnit.toMillis(callTimeout)
            return this
        }

        /**
         * Sets a custom SSL socket factory for HTTPS connections.
         *
         * @param sslSocketFactory Custom SSL socket factory, or null to use default
         * @return This builder instance for method chaining
         */
        fun withSslSocketFactory(sslSocketFactory: SSLSocketFactory?): Builder {
            this.sslSocketFactory = sslSocketFactory
            return this
        }

        /**
         * Sets whether to use caching for HTTP connections.
         *
         * @param useCaches true to enable caching, false to disable
         * @return This builder instance for method chaining
         */
        fun withUseCaches(useCaches: Boolean): Builder {
            this.useCaches = useCaches
            return this
        }

        /**
         * Sets whether to follow HTTP redirects automatically.
         *
         * @param instanceFollowRedirects true to follow redirects, false to disable
         * @return This builder instance for method chaining
         */
        fun withInstanceFollowRedirects(instanceFollowRedirects: Boolean): Builder {
            this.instanceFollowRedirects = instanceFollowRedirects
            return this
        }

        /**
         * Sets the maximum size of response data.
         *
         * @param maxResponseSize Maximum response size in bytes
         * @return This builder instance for method chaining
         */
        fun withMaxResponseSize(maxResponseSize: Int): Builder {
            this.maxResponseSize = maxResponseSize
            return this
        }

        /**
         * Enables or disables collection of [NetworkCallMetrics] during call execution.
         * When enabled, the resulting [Response] will have [Response.metrics] populated.
         *
         * @param collectMetrics true to collect metrics, false to disable
         * @return This builder instance for method chaining
         */
        fun withCollectMetrics(collectMetrics: Boolean): Builder {
            this.collectMetrics = collectMetrics
            return this
        }

        /**
         * Builds a new [NetworkClientSettings] instance with the configured settings.
         *
         * @return A new network client settings instance
         */
        fun build() = NetworkClientSettings(
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
            callTimeout = callTimeout,
            sslSocketFactory = sslSocketFactory,
            useCaches = useCaches,
            instanceFollowRedirects = instanceFollowRedirects,
            maxResponseSize = maxResponseSize,
            collectMetrics = collectMetrics,
        )
    }
}
