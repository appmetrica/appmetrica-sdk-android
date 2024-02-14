package io.appmetrica.analytics.impl.modules

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import io.appmetrica.analytics.logger.internal.YLogger

class ServiceWakeLockBinder(private val intentProvider: ServiceWakeLockIntentProvider) {
    private val tag = "[ServiceWakeLockBinder]"

    fun bindService(context: Context, action: String): ServiceConnection? {
        val intent = intentProvider.getWakeLockIntent(context, action)
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                YLogger.info(tag, "Service connected for action = $action")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                YLogger.info(tag, "Service disconnected for action = $action")
            }
        }
        try {
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            return serviceConnection
        } catch (e: Throwable) {
            YLogger.error(tag, e, "Failed bind to service with action = $action")
        }
        return null
    }

    fun unbindService(action: String, context: Context, serviceConnection: ServiceConnection) {
        try {
            context.unbindService(serviceConnection)
        } catch (e: Throwable) {
            YLogger.error(tag, e, "Failed unbind to service with action = $action")
        }
    }
}
