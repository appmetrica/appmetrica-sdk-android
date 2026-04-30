package io.appmetrica.analytics.impl.referrer.service.provider.rustore

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback
import io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

/**
 * Binder client for RuStore install referrer service.
 *
 * Connects to the original RuStore service via Intent action
 * "ru.vk.store.sdk.install.referrer.InstallReferrerProvider" using [InstallReferrerProvider]
 * and [GetInstallReferrerCallback] with the original RuStore DESCRIPTORs to avoid
 * enforceInterface() failures.
 */
internal class RuStoreReferrerService(private val context: Context) {

    private val tag = "[RuStoreReferrerService]"

    /**
     * The original DESCRIPTOR / Intent action from RuStore SDK.
     * We must use this exact string to find and bind to the RuStore service.
     */
    private val ruStoreServiceAction = "ru.vk.store.sdk.install.referrer.InstallReferrerProvider"
    private val ruStorePackageName = "ru.vk.store"

    fun requestReferrer(listener: ReferrerListener) {
        val component = findRuStoreComponent()
        if (component == null) {
            val message = "RuStore service component not found"
            DebugLogger.info(tag, message)
            listener.onResult(ReferrerResult.Failure(message))
            return
        }

        val intent = Intent(ruStoreServiceAction).apply {
            setComponent(component)
        }

        val connection = RuStoreServiceConnection(listener)

        val bound = try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Throwable) {
            val message = "Failed to bind RuStore service"
            DebugLogger.warning(tag, "$message: ${e.message}")
            listener.onResult(ReferrerResult.Failure(message, e))
            return
        }

        if (!bound) {
            val message = "bindService returned false for RuStore service: $component"
            DebugLogger.warning(tag, message)
            listener.onResult(ReferrerResult.Failure(message))
        }
    }

    private inner class RuStoreServiceConnection(
        private val listener: ReferrerListener,
    ) : ServiceConnection {

        // Held as a field so the Binder callback is not garbage-collected before
        // RuStore calls onSuccess/onError from the remote process.
        private val referrerCallback = object : GetInstallReferrerCallback.Stub() {
            override fun onSuccess(payload: String?) {
                DebugLogger.info(tag, "Got referrer from RuStore: $payload")
                unbindSafely(this@RuStoreServiceConnection)
                listener.onResult(parsePayload(payload))
            }

            override fun onError(code: Int, errorMessage: String) {
                DebugLogger.warning(tag, "RuStore referrer error $code: $errorMessage")
                unbindSafely(this@RuStoreServiceConnection)
                listener.onResult(ReferrerResult.Failure("RuStore referrer error $code: $errorMessage"))
            }
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            DebugLogger.info(tag, "Service connected: $name")
            try {
                val service = InstallReferrerProvider.Stub.asInterface(binder)
                if (service == null) {
                    val message = "RuStore service binder is null"
                    DebugLogger.warning(tag, message)
                    unbindSafely(this)
                    listener.onResult(ReferrerResult.Failure(message))
                    return
                }
                service.getInstallReferrer(context.packageName, referrerCallback)
            } catch (e: Throwable) {
                val message = "Failed to referrer from RuStore service"
                DebugLogger.warning(tag, "$message: ${e.message}")
                unbindSafely(this)
                listener.onResult(ReferrerResult.Failure(message, e))
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            DebugLogger.warning(tag, "Service disconnected unexpectedly: $name")
        }

        override fun onBindingDied(name: ComponentName?) {
            DebugLogger.warning(tag, "Binding died: $name")
            val message = "RuStore service binding died"
            unbindSafely(this)
            listener.onResult(ReferrerResult.Failure(message))
        }

        override fun onNullBinding(name: ComponentName?) {
            DebugLogger.warning(tag, "Null binding from RuStore service: $name")
            val message = "RuStore service returned null binding"
            unbindSafely(this)
            listener.onResult(ReferrerResult.Failure(message))
        }
    }

    private fun parsePayload(payload: String?): ReferrerResult {
        DebugLogger.info(tag, "RuStore return referrer: $payload")
        if (payload.isNullOrBlank()) {
            return ReferrerResult.Failure("RuStore referrer payload is empty")
        }
        return try {
            val json = JSONObject(payload)
            val referrerId = json.getString("REFERRER_ID_KEY")
            if (referrerId.isBlank()) {
                return ReferrerResult.Failure("RuStore referrer id is empty")
            }
            // RuStore provides timestamps in milliseconds (confirmed by RuStore SDK source);
            // AppMetrica stores and reports timestamps in seconds, so we divide by 1000.
            val referrerClickTimestampSeconds = json.optLong("RECEIVED_TIMESTAMP_KEY") / 1000
            val installBeginTimestampSeconds = json.optLong("INSTALL_APP_TIMESTAMP_KEY") / 1000
            ReferrerResult.Success(
                ReferrerInfo(
                    referrerId,
                    referrerClickTimestampSeconds,
                    installBeginTimestampSeconds,
                    ReferrerInfo.Source.RS,
                )
            )
        } catch (e: Throwable) {
            val message = "Failed to parse RuStore referrer payload"
            DebugLogger.warning(tag, "$message: ${e.message}")
            ReferrerResult.Failure(message, e)
        }
    }

    private fun findRuStoreComponent(): ComponentName? {
        return try {
            val intent = Intent(ruStoreServiceAction).apply {
                `package` = ruStorePackageName
            }
            val services = context.packageManager.queryIntentServices(intent, 0)
            if (services.isEmpty()) {
                DebugLogger.info(tag, "No RuStore service found for action $ruStoreServiceAction")
                null
            } else {
                val service = services.first()
                val component = ComponentName(service.serviceInfo.packageName, service.serviceInfo.name)
                DebugLogger.info(tag, "Found RuStore service component: $component")
                component
            }
        } catch (e: Throwable) {
            DebugLogger.warning(tag, "Failed to query RuStore service: ${e.message}")
            null
        }
    }

    private fun unbindSafely(connection: ServiceConnection) {
        try {
            context.unbindService(connection)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Failed to unbind RuStore service")
        }
    }
}
