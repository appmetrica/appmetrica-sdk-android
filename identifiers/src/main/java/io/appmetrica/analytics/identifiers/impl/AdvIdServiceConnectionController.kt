package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceAccessDeniedException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceBindingException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceCommunicationException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceConnectionTimeoutException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceNotFoundException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceResponseException
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private const val ATTEMPT_TIMEOUT = 3000L

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

    fun connect(context: Context): T {
        val intent = connection.intent
        DebugLogger.info(tag, "Begin establish connection to service: %s...", intent)
        if (safePackageManager.resolveService(context, intent, 0) == null) {
            throw AdvIdServiceNotFoundException("could not resolve $serviceShortTag services")
        }
        DebugLogger.info(tag, "Intent (%s) resolved. Begin binding...", intent)
        try {
            DebugLogger.info(tag, "Bind with intent = %s...", intent)
            val status = connection.bindService(context)
            DebugLogger.info(tag, "Bind with intent = %s... Status = %b", intent, status)
            if (!status) {
                throw AdvIdServiceBindingException("could not bind to $serviceShortTag services")
            }
            DebugLogger.info(
                tag,
                "Binding... Wait connection or binding for %d ms...",
                ATTEMPT_TIMEOUT
            )
            val service = connection.awaitBinding(ATTEMPT_TIMEOUT)
            DebugLogger.info(tag, "Binding... Service after waiting: %s", service)
            if (service == null) {
                throw connectionExceptionFor(connection.bindingState)
            }
            return converter(service) ?: throw AdvIdServiceResponseException(
                "$serviceShortTag service returned invalid binder"
            )
        } catch (exception: SecurityException) {
            throw AdvIdServiceAccessDeniedException(
                "access to $serviceShortTag service denied",
                exception
            )
        }
    }

    private fun connectionExceptionFor(state: AdvIdServiceConnection.BindingState?): RuntimeException {
        return when (state) {
            AdvIdServiceConnection.BindingState.NULL_BINDING -> AdvIdServiceResponseException(
                "$serviceShortTag service returned invalid binder"
            )
            AdvIdServiceConnection.BindingState.BINDING_DIED,
            AdvIdServiceConnection.BindingState.DISCONNECTED -> AdvIdServiceCommunicationException(
                "connection to $serviceShortTag service was lost"
            )
            AdvIdServiceConnection.BindingState.INTERRUPTED -> AdvIdServiceConnectionTimeoutException(
                "connection to $serviceShortTag service interrupted"
            )
            else -> AdvIdServiceConnectionTimeoutException(
                "connection to $serviceShortTag service timed out"
            )
        }
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
