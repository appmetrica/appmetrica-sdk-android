package io.appmetrica.analytics.networkapi

/**
 * Represents an HTTP response.
 * Use [Builder] to construct instances of this class.
 *
 * @property isCompleted Whether the request completed successfully without exceptions
 * @property code HTTP response code (e.g., 200, 404, 500)
 * @property responseData Response body as byte array
 * @property headers HTTP response headers as key-value pairs with multiple values per key
 * @property exception Exception that occurred during request execution, or null if successful
 * @property url Final URL after any redirects, or null if not available
 */
class Response private constructor(
    val isCompleted: Boolean,
    val code: Int,
    val responseData: ByteArray,
    val headers: Map<String, List<String>>,
    val exception: Throwable?,
    val url: String?,
) {

    override fun toString(): String {
        return "Response(" +
            "isCompleted=$isCompleted, " +
            "code=$code, " +
            "responseDataLength=${responseData.size}, " +
            "headers=$headers, " +
            "exception=$exception, " +
            "url=$url" +
            ")"
    }

    /**
     * Builder for creating [Response] instances.
     * Provides constructors for both successful responses and error responses.
     */
    class Builder private constructor(
        private val isCompleted: Boolean,
        private val code: Int,
        private val responseData: ByteArray,
        private val exception: Throwable?,
    ) {

        private var headers: Map<String, List<String>> = emptyMap()
        private var url: String? = null

        /**
         * Creates a builder for an error response with an exception.
         *
         * @param exception Exception that occurred during request execution
         */
        constructor(
            exception: Throwable?
        ) : this(
            isCompleted = false,
            code = 0,
            responseData = ByteArray(0),
            exception = exception,
        )

        /**
         * Creates a builder for a response with status code and data.
         *
         * @param isCompleted Whether the request completed successfully
         * @param code HTTP response code
         * @param responseData Response body as byte array
         */
        constructor(
            isCompleted: Boolean,
            code: Int,
            responseData: ByteArray,
        ) : this(
            isCompleted = isCompleted,
            code = code,
            responseData = responseData,
            exception = null,
        )

        /**
         * Sets the response headers.
         *
         * @param headers HTTP response headers as key-value pairs with multiple values per key
         * @return This builder instance for method chaining
         */
        fun withHeaders(headers: Map<String, List<String>>): Builder {
            this.headers = headers.toMap()
            return this
        }

        /**
         * Sets the final URL after any redirects.
         *
         * @param url Final URL
         * @return This builder instance for method chaining
         */
        fun withUrl(url: String): Builder {
            this.url = url
            return this
        }

        /**
         * Builds a new [Response] instance with the configured parameters.
         *
         * @return A new HTTP response instance
         */
        fun build(): Response {
            return Response(
                isCompleted = isCompleted,
                code = code,
                responseData = responseData,
                headers = headers,
                exception = exception,
                url = url,
            )
        }
    }
}
