package io.appmetrica.analytics.impl.service

import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.impl.AppMetricaServiceCore
import io.appmetrica.analytics.impl.AppMetricaServiceCoreImpl
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.SelfProcessReporter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

class AppMetricaServiceProxy(
    private val context: Context,
    private val serviceCallback: AppMetricaServiceCallback
) : AppMetricaServiceDelegate {

    private companion object {
        private const val TAG = "[AppMetricaServiceProxy]"

        private var serviceCore: AppMetricaServiceCore? = null

        private fun registerAppMetricaServiceCoreIfNeed(
            context: Context,
            serviceCallback: AppMetricaServiceCallback
        ) {
            if (serviceCore == null) {
                val core = AppMetricaServiceCoreImpl(context, serviceCallback)
                GlobalServiceLocator.getInstance().serviceDataReporterHolder.registerServiceDataReporter(
                    AppMetricaServiceDataReporter.TYPE_CORE,
                    AppMetricaServiceDataReporter(core)
                )
                serviceCore = core
            }
        }
    }

    private lateinit var coreBinder: Binder

    override fun onCreate() {
        DebugLogger.info(TAG, "AppMetricaService#onCreate()")
        GlobalServiceLocator.init(context)
        PublicLogger.init(context)
        DebugLogger.info(
            TAG,
            "Service proxy was created for owner with package ${context.packageName}"
        )
        registerAppMetricaServiceCoreIfNeed(context, serviceCallback)
        serviceCore?.let {
            coreBinder = AppMetricaServiceBinder(it)
            GlobalServiceLocator.getInstance()
                .initSelfDiagnosticReporterStorage(SelfProcessReporter(it))
            it.onCreate()
        }
    }

    override fun onStart(intent: Intent, startId: Int) {
        DebugLogger.info(TAG, "AppMetricaService#onStart: $intent")
        serviceCore?.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        DebugLogger.info(TAG, "AppMetricaService#onStartCommand: $intent")
        serviceCore?.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        DebugLogger.info(TAG, "Bind to the service with data: $intent")
        serviceCore?.onBind(intent)
        return if (intent.isWakelockAction()) WakeLockBinder() else coreBinder
    }

    override fun onRebind(intent: Intent) {
        DebugLogger.info(TAG, "Rebind to service with data: $intent")
        serviceCore?.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        DebugLogger.info(TAG, "Unbind from the service with data: $intent")
        serviceCore?.onUnbind(intent)
        if (intent.isWakelockAction()) {
            DebugLogger.info(TAG, "Wakelock action")
            return false
        }
        if (intent.isInvalidIntentData()) {
            DebugLogger.info(TAG, "Invalid intent data")
            return true
        }
        return false
    }

    override fun onDestroy() {
        DebugLogger.info(TAG, "Service has been destroyed")
        serviceCore?.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        DebugLogger.info(TAG, "Service configuration changed")
        serviceCore?.onConfigurationChanged(newConfig)
    }

    private fun Intent.isWakelockAction(): Boolean {
        return action?.startsWith(AppMetricaConnectionConstants.ACTION_SERVICE_WAKELOCK) == true
    }

    private fun Intent?.isInvalidIntentData(): Boolean = this == null || this.data == null

    @VisibleForTesting
    fun reset() {
        serviceCore = null
    }
}
