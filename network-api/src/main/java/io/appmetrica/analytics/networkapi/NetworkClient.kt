package io.appmetrica.analytics.networkapi

/**
 * Abstract network client for executing HTTP requests.
 * Implementations of this class should provide concrete network communication logic.
 *
 * @property settings Configuration settings for the network client
 */
abstract class NetworkClient(
    val settings: NetworkClientSettings,
) {

    /**
     * Creates a new network call that can be executed to perform the given [request].
     *
     * @param request The HTTP request to be executed
     * @return A [Call] object that can be executed to get a response
     */
    abstract fun newCall(request: Request): Call

    /**
     * Abstract builder for creating [NetworkClient] instances.
     * Implementations should provide specific network client configuration.
     */
    abstract class Builder {

        /**
         * Network client settings. Default settings are used if not explicitly configured.
         */
        protected var settings: NetworkClientSettings = NetworkClientSettings.Builder().build()
            private set

        /**
         * Configures the network client with the given settings.
         *
         * @param settings Configuration settings to apply
         * @return This builder instance for method chaining
         */
        fun withSettings(settings: NetworkClientSettings): Builder {
            this.settings = settings
            return this
        }

        /**
         * Builds and returns a new [NetworkClient] instance with the configured settings.
         *
         * @return A new network client instance
         */
        abstract fun build(): NetworkClient
    }
}
