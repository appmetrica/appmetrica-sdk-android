package io.appmetrica.analytics.networkapi

import javax.net.ssl.SSLSocketFactory

/**
 * Configuration settings for [NetworkClient].
 * Use [Builder] to construct instances of this class.
 *
 * @property connectTimeout Connection timeout in milliseconds, or null to use default
 * @property readTimeout Read timeout in milliseconds, or null to use default
 * @property sslSocketFactory Custom SSL socket factory for HTTPS connections, or null to use default
 * @property useCaches Whether to use caching for HTTP connections, or null to use default
 * @property instanceFollowRedirects Whether to follow HTTP redirects automatically, or null to use default
 * @property maxResponseSize Maximum size of response data in bytes
 */
class NetworkClientSettings private constructor(
    val connectTimeout: Int?,
    val readTimeout: Int?,
    val sslSocketFactory: SSLSocketFactory?,
    val useCaches: Boolean?,
    val instanceFollowRedirects: Boolean?,
    val maxResponseSize: Int,
) {

    override fun toString(): String {
        return "NetworkClientSettings(" +
            "connectTimeout=$connectTimeout, " +
            "readTimeout=$readTimeout, " +
            "sslSocketFactory=$sslSocketFactory, " +
            "useCaches=$useCaches, " +
            "instanceFollowRedirects=$instanceFollowRedirects, " +
            "maxResponseSize=$maxResponseSize" +
            ")"
    }

    /**
     * Builder for creating [NetworkClientSettings] instances.
     * Provides a fluent API for configuring network client settings.
     */
    class Builder {

        private var connectTimeout: Int? = null
        private var readTimeout: Int? = null
        private var sslSocketFactory: SSLSocketFactory? = null
        private var useCaches: Boolean? = null
        private var instanceFollowRedirects: Boolean? = null
        private var maxResponseSize: Int = Int.MAX_VALUE

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
         * Builds a new [NetworkClientSettings] instance with the configured settings.
         *
         * @return A new network client settings instance
         */
        fun build() = NetworkClientSettings(
            connectTimeout,
            readTimeout,
            sslSocketFactory,
            useCaches,
            instanceFollowRedirects,
            maxResponseSize,
        )
    }
}
