package io.appmetrica.analytics.impl.service

import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
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

    private val tag = "[AppMetricaServiceProxy]"

    private lateinit var serviceCore: AppMetricaServiceCore
    private lateinit var coreBinder: Binder

    override fun onCreate() {
        DebugLogger.info(tag, "AppMetricaService#onCreate()")
        GlobalServiceLocator.init(context)
        PublicLogger.init(context)
        DebugLogger.info(tag, "Service proxy was created for owner with package ${context.packageName}")
        if (!this::serviceCore.isInitialized) {
            val core = AppMetricaServiceCoreImpl(context, serviceCallback)
            GlobalServiceLocator.getInstance().serviceDataReporterHolder.registerServiceDataReporter(
                AppMetricaServiceDataReporter.TYPE_CORE,
                AppMetricaServiceDataReporter(core)
            )
            serviceCore = core
            coreBinder = AppMetricaServiceBinder(serviceCore)
        }
        GlobalServiceLocator.getInstance().initSelfDiagnosticReporterStorage(SelfProcessReporter(serviceCore))
        serviceCore.onCreate()
    }

    override fun onStart(intent: Intent, startId: Int) {
        DebugLogger.info(tag, "AppMetricaService#onStart: $intent")
        serviceCore.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        DebugLogger.info(tag, "AppMetricaService#onStartCommand: $intent")
        serviceCore.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        DebugLogger.info(tag, "Bind to the service with data: $intent")
        serviceCore.onBind(intent)
        return if (intent.isWakelockAction()) WakeLockBinder() else coreBinder
    }

    override fun onRebind(intent: Intent) {
        DebugLogger.info(tag, "Rebind to service with data: $intent")
        serviceCore.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        DebugLogger.info(tag, "Unbind from the service with data: $intent")
        serviceCore.onUnbind(intent)
        if (intent.isWakelockAction()) {
            DebugLogger.info(tag, "Wakelock action")
            return false
        }
        if (intent.isInvalidIntentData()) {
            DebugLogger.info(tag, "Invalid intent data")
            return true
        }
        return false
    }

    override fun onDestroy() {
        DebugLogger.info(tag, "Service has been destroyed")
        serviceCore.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        DebugLogger.info(tag, "Service configuration changed")
        serviceCore.onConfigurationChanged(newConfig)
    }

    private fun Intent.isWakelockAction(): Boolean {
        return action?.startsWith(AppMetricaConnectionConstants.ACTION_SERVICE_WAKELOCK) == true
    }

    private fun Intent?.isInvalidIntentData(): Boolean = this == null || this.data == null
}
