package io.appmetrica.analytics.networkapi

/**
 * Represents an HTTP request.
 * Use [Builder] to construct instances of this class.
 *
 * @property url The target URL for the request
 * @property method The HTTP method (GET or POST)
 * @property body Request body as byte array
 * @property headers HTTP headers as key-value pairs
 */
class Request private constructor(
    val url: String,
    val method: Method,
    val body: ByteArray,
    val headers: Map<String, String>,
) {

    /**
     * HTTP method type.
     */
    enum class Method(
        val methodName: String,
    ) {
        /** HTTP GET method */
        GET("GET"),

        /** HTTP POST method */
        POST("POST"),
    }

    override fun toString(): String {
        return "Request(" +
            "url='$url', " +
            "method='$method', " +
            "bodyLength=${body.size}, " +
            "headers=$headers" +
            ")"
    }

    /**
     * Builder for creating [Request] instances.
     * Provides a fluent API for configuring HTTP requests.
     *
     * @param url The target URL for the request
     */
    class Builder(private val url: String) {

        private var method = Method.GET
        private var body = ByteArray(0)
        private val headers = mutableMapOf<String, String>()

        /**
         * Adds an HTTP header to the request.
         *
         * @param key Header name
         * @param value Header value
         * @return This builder instance for method chaining
         */
        fun addHeader(key: String, value: String): Builder {
            headers.put(key, value)
            return this
        }

        /**
         * Sets the request body.
         *
         * @param body Request body as byte array
         * @return This builder instance for method chaining
         */
        fun withBody(body: ByteArray): Builder {
            this.body = body
            return this
        }

        /**
         * Sets the HTTP method.
         *
         * @param method HTTP method (GET or POST)
         * @return This builder instance for method chaining
         */
        fun withMethod(method: Method): Builder {
            this.method = method
            return this
        }

        /**
         * Builds a new [Request] instance with the configured parameters.
         *
         * @return A new HTTP request instance
         */
        fun build(): Request {
            return Request(url, method, body, headers.toMap())
        }
    }
}
