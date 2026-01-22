package io.appmetrica.analytics.impl.service

import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder

internal interface AppMetricaServiceDelegate {

    fun onCreate()
    fun onStart(intent: Intent, startId: Int)
    fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    fun onBind(intent: Intent): IBinder
    fun onRebind(intent: Intent)
    fun onUnbind(intent: Intent): Boolean
    fun onDestroy()
    fun onConfigurationChanged(newConfig: Configuration)
}
