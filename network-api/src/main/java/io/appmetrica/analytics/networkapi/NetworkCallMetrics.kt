package io.appmetrica.analytics.networkapi

/**
 * Network timing metrics collected during a single HTTP call execution.
 *
 * All durations are in milliseconds. Fields are null when the corresponding
 * phase did not occur (e.g. [dnsLookup] is null when a connection was reused).
 *
 * Use [Builder] to construct instances of this class.
 *
 * @property dnsLookup DNS resolution time in milliseconds, or null if DNS phase was skipped (connection reused)
 * @property tcpConnect TCP connection establishment time in milliseconds, or null if skipped
 * @property tlsHandshake TLS handshake time in milliseconds, or null for plain HTTP or reused connections
 * @property timeToFirstByte Time in milliseconds from sending the request to receiving the first response byte
 * @property response Time in milliseconds to fully read the response body
 * @property connectionReused Whether an existing connection was reused for this call
 * @property protocol Protocol used for the connection (e.g. "h2", "http/1.1"), or null if unknown
 */
class NetworkCallMetrics private constructor(
    val dnsLookup: Long?,
    val tcpConnect: Long?,
    val tlsHandshake: Long?,
    val timeToFirstByte: Long?,
    val response: Long?,
    val connectionReused: Boolean,
    val protocol: String?,
) {

    override fun toString(): String = "NetworkCallMetrics(" +
        "dnsLookup=$dnsLookup, " +
        "tcpConnect=$tcpConnect, " +
        "tlsHandshake=$tlsHandshake, " +
        "timeToFirstByte=$timeToFirstByte, " +
        "response=$response, " +
        "connectionReused=$connectionReused, " +
        "protocol=$protocol" +
        ")"

    /**
     * Builder for creating [NetworkCallMetrics] instances.
     */
    class Builder {

        private var dnsLookup: Long? = null
        private var tcpConnect: Long? = null
        private var tlsHandshake: Long? = null
        private var timeToFirstByte: Long? = null
        private var response: Long? = null
        private var connectionReused: Boolean = false
        private var protocol: String? = null

        /**
         * Sets the DNS resolution time.
         *
         * @param dnsLookup DNS lookup duration in milliseconds, or null if phase was skipped
         * @return This builder instance for method chaining
         */
        fun withDnsLookup(dnsLookup: Long?): Builder {
            this.dnsLookup = dnsLookup
            return this
        }

        /**
         * Sets the TCP connection establishment time.
         *
         * @param tcpConnect TCP connect duration in milliseconds, or null if phase was skipped
         * @return This builder instance for method chaining
         */
        fun withTcpConnect(tcpConnect: Long?): Builder {
            this.tcpConnect = tcpConnect
            return this
        }

        /**
         * Sets the TLS handshake time.
         *
         * @param tlsHandshake TLS handshake duration in milliseconds, or null for plain HTTP or reused connections
         * @return This builder instance for method chaining
         */
        fun withTlsHandshake(tlsHandshake: Long?): Builder {
            this.tlsHandshake = tlsHandshake
            return this
        }

        /**
         * Sets the time from sending the request to receiving the first response byte.
         *
         * @param timeToFirstByte Time to first byte in milliseconds, or null if not measured
         * @return This builder instance for method chaining
         */
        fun withTimeToFirstByte(timeToFirstByte: Long?): Builder {
            this.timeToFirstByte = timeToFirstByte
            return this
        }

        /**
         * Sets the time to fully read the response body.
         *
         * @param response Response read duration in milliseconds, or null if not measured
         * @return This builder instance for method chaining
         */
        fun withResponse(response: Long?): Builder {
            this.response = response
            return this
        }

        /**
         * Sets whether an existing connection was reused for this call.
         *
         * @param connectionReused true if the connection was reused, false otherwise
         * @return This builder instance for method chaining
         */
        fun withConnectionReused(connectionReused: Boolean): Builder {
            this.connectionReused = connectionReused
            return this
        }

        /**
         * Sets the protocol used for the connection.
         *
         * @param protocol Protocol string (e.g. "h2", "http/1.1"), or null if unknown
         * @return This builder instance for method chaining
         */
        fun withProtocol(protocol: String?): Builder {
            this.protocol = protocol
            return this
        }

        /**
         * Builds a new [NetworkCallMetrics] instance with the configured parameters.
         *
         * @return A new network call metrics instance
         */
        fun build() = NetworkCallMetrics(
            dnsLookup = dnsLookup,
            tcpConnect = tcpConnect,
            tlsHandshake = tlsHandshake,
            timeToFirstByte = timeToFirstByte,
            response = response,
            connectionReused = connectionReused,
            protocol = protocol,
        )
    }
}
