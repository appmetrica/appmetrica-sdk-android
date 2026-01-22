package io.appmetrica.analytics.impl.service

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/* ktlint-disable appmetrica-rules:internal-modifier-in-impl-package */
open class AppMetricaCoreService : Service() {

    private val tag = "[AppMetricaCoreService]"

    private lateinit var serviceDelegate: AppMetricaServiceDelegate

    override fun onCreate() {
        super.onCreate()
        DebugLogger.info(tag, "onCreate")
        if (!this::serviceDelegate.isInitialized) {
            DebugLogger.info(tag, "Create service delegate")
            serviceDelegate = AppMetricaServiceProxy(this, AppMetricaCoreServiceCallback(this))
        }
        serviceDelegate.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        DebugLogger.info(tag, "onStartCommand")
        return serviceDelegate.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        DebugLogger.info(tag, "onBind")
        return serviceDelegate.onBind(intent)
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        DebugLogger.info(tag, "onRebind")
        serviceDelegate.onRebind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugLogger.info(tag, "onDestroy")
        serviceDelegate.onDestroy()
    }

    override fun onUnbind(intent: Intent): Boolean {
        DebugLogger.info(tag, "onUnbind")
        return serviceDelegate.onUnbind(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        DebugLogger.info(tag, "onConfigurationChanged")
        serviceDelegate.onConfigurationChanged(newConfig)
    }
}
