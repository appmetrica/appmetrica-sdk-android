package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.logger.internal.DebugLogger

internal const val ATTEMPT_TIMEOUT = 3000L

internal class AdvIdServiceConnectionController<T> @VisibleForTesting internal constructor(
    @get:VisibleForTesting
    val connection: AdvIdServiceConnection,
    private val converter: (IBinder) -> T,
    private val tag: String,
    private val serviceShortTag: String,
    private val safePackageManager: SafePackageManager,
) {

    constructor(
        intent: Intent,
        converter: (IBinder) -> T,
        serviceShortTag: String
    ) : this(
        AdvIdServiceConnection(intent, serviceShortTag),
        converter,
        "[AdvIdServiceConnectionController-$serviceShortTag]",
        serviceShortTag,
        SafePackageManager()
    )

    @Throws(ConnectionException::class)
    fun connect(context: Context): T {
        val intent = connection.intent
        DebugLogger.info(tag, "Begin establish connection to service: %s...", intent)
        if (safePackageManager.resolveService(context, intent, 0) == null) {
            throw NoProviderException("could not resolve $serviceShortTag services")
        }
        DebugLogger.info(tag, "Intent (%s) resolved. Begin binding...", intent)
        var service: IBinder? = null
        try {
            DebugLogger.info(tag, "Bind with intent = %s...", intent)
            val status = connection.bindService(context)
            DebugLogger.info(tag, "Bind with intent = %s... Status = %b", intent, status)
            if (status) {
                DebugLogger.info(
                    tag,
                    "Binding... Wait connection or binding for %d ms...",
                    ATTEMPT_TIMEOUT
                )
                service = connection.awaitBinding(3000L)
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        }
        DebugLogger.info(tag, "Binding... Service after waiting: %s", service)
        if (service == null) {
            throw ConnectionException("could not bind to $serviceShortTag services")
        }
        return converter(service)
    }

    fun disconnect(context: Context) {
        try {
            connection.unbindService(context)
            DebugLogger.info(tag, "Unbind from %s successful", connection.intent)
        } catch (ignored: IllegalArgumentException) {
        } catch (e: Throwable) {
            DebugLogger.error(
                tag, e, "Could not unbind from service with intent = %s", connection.intent
            )
        }
    }
}

internal open class ConnectionException(message: String?) : Exception(message)

internal class NoProviderException(message: String?) : ConnectionException(message)
