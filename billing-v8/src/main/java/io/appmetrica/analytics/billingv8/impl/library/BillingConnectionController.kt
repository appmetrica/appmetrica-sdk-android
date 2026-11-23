package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billingv8.impl.BillingUtils
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Call [request] and [disable] on the supplied worker executor.
 * BillingClient callbacks are posted to the same executor before accessing controller state.
 */
internal class BillingConnectionController(
    private val workerExecutor: IHandlerExecutor,
    private val createClient: () -> BillingClient,
    private val collect: (BillingConfig, BillingClient, BillingLibraryConnectionHolder) -> Unit,
) {

    private var pendingConfig: BillingConfig? = null
    private var connection: Connection? = null
    private var lastConnectionId = 0L

    @WorkerThread
    fun request(config: BillingConfig) {
        if (pendingConfig != null) {
            DebugLogger.info(MODULE_TAG, "Replacing pending billing refresh with the latest request")
        }
        pendingConfig = config
        processPendingRefresh()
    }

    @WorkerThread
    fun disable() {
        pendingConfig = null
        connection?.let { close(it, "billing requests disabled by configuration") }
    }

    @WorkerThread
    private fun processPendingRefresh() {
        val current = connection
        val config = pendingConfig
        when {
            current == null -> if (config != null) connect(config)
            current.isAwaitingSetupResult() -> {
                DebugLogger.info(MODULE_TAG, "Billing refresh waiting for setup, connectionId=${current.id}")
            }
            current.hasActiveCollection() -> {
                DebugLogger.info(
                    MODULE_TAG,
                    "Billing refresh deferred until previous collection finishes, connectionId=${current.id}"
                )
            }
            config == null -> close(current, "no pending billing refresh requests")
            checkClientReady(current) -> collectNext(current)
            else -> fail(current, "client not ready for pending billing refresh")
        }
    }

    private fun connect(config: BillingConfig) {
        // Only the request that initiated setup is consumed if setup fails.
        pendingConfig = null
        val client = try {
            createClient()
        } catch (e: Throwable) {
            DebugLogger.error(MODULE_TAG, e, "Failed to create BillingClient")
            return
        }
        lastConnectionId++
        val current = Connection(lastConnectionId, client, config)
        connection = current
        DebugLogger.info(MODULE_TAG, "Starting BillingClient setup, connectionId=${current.id}")
        try {
            client.startConnection(createListener(current))
        } catch (e: Throwable) {
            DebugLogger.error(MODULE_TAG, e, "Failed to start BillingClient connection, connectionId=${current.id}")
            fail(current, "startConnection failed")
        }
    }

    private fun createListener(current: Connection) = object : BillingClientStateListener {
        override fun onBillingSetupFinished(result: BillingResult) = post {
            if (connection !== current || !current.isAwaitingSetupResult()) {
                DebugLogger.info(
                    MODULE_TAG,
                    "Ignoring stale or duplicate billing setup callback, connectionId=${current.id}"
                )
                return@post
            }
            DebugLogger.info(
                MODULE_TAG,
                "onBillingSetupFinished result=${BillingUtils.toString(result)}, connectionId=${current.id}"
            )
            when {
                result.responseCode != BillingClient.BillingResponseCode.OK -> fail(current, "setup failed")
                !checkClientReady(current) -> fail(current, "client not ready after successful setup")
                else -> collectNext(current)
            }
        }

        override fun onBillingServiceDisconnected() = post {
            if (connection === current) {
                fail(current, "billing service disconnected")
            }
        }
    }

    private fun checkClientReady(current: Connection): Boolean = try {
        current.client.isReady
    } catch (e: Throwable) {
        DebugLogger.error(MODULE_TAG, e, "Failed to read BillingClient state, connectionId=${current.id}")
        false
    }

    @WorkerThread
    private fun collectNext(current: Connection) {
        val config = pendingConfig ?: checkNotNull(current.setupConfig)
        pendingConfig = null
        current.setupConfig = null
        val round = BillingLibraryConnectionHolder { success ->
            post { finishRound(current, success) }
        }
        current.round = round
        DebugLogger.info(MODULE_TAG, "Starting billing collection, connectionId=${current.id}")
        try {
            collect(config, current.client, round)
        } catch (e: Throwable) {
            DebugLogger.error(MODULE_TAG, e, "Failed to collect billing purchases, connectionId=${current.id}")
            fail(current, "collection failed to start")
        }
    }

    private fun finishRound(current: Connection, success: Boolean) {
        if (connection !== current) return
        current.round = null
        DebugLogger.info(MODULE_TAG, "Billing collection finished, connectionId=${current.id}, success=$success")
        if (!success) close(current, "collection completed with errors")
        processPendingRefresh()
    }

    private fun fail(current: Connection, reason: String) {
        close(current, reason)
        processPendingRefresh()
    }

    private fun close(current: Connection, reason: String) {
        DebugLogger.info(MODULE_TAG, "Closing BillingClient, connectionId=${current.id}, reason=$reason")
        current.round?.cancel()
        connection = null
        try {
            current.client.endConnection()
            DebugLogger.info(MODULE_TAG, "endConnection returned, connectionId=${current.id}")
        } catch (e: Throwable) {
            DebugLogger.error(MODULE_TAG, e, "endConnection failed, connectionId=${current.id}")
        }
    }

    private fun post(action: () -> Unit) {
        workerExecutor.post(object : SafeRunnable() {
            override fun runSafety() = action()
        })
    }

    private class Connection(val id: Long, val client: BillingClient, var setupConfig: BillingConfig?) {
        var round: BillingLibraryConnectionHolder? = null

        fun isAwaitingSetupResult(): Boolean = setupConfig != null

        fun hasActiveCollection(): Boolean = round != null
    }
}
