package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.content.Context
import com.applovin.communicator.AppLovinCommunicator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppLovinIlrdAdapter(
    private val context: Context,
    private val reporter: AppLovinIlrdReporter,
) {

    private val tag = "[AppLovinIlrdAdapter]"

    private var subscriber: AppLovinIlrdSubscriber? = null

    fun registerSubscriber(): Boolean {
        synchronized(this) {
            if (subscriber != null) {
                DebugLogger.info(tag, "registerSubscriber: already registered, skipping")
                return true
            }
            val sub = AppLovinIlrdSubscriber(reporter::onIlrdReceived)
            return try {
                AppLovinCommunicator.getInstance(context).subscribe(sub, Constants.TOPIC)
                DebugLogger.info(tag, "AppLovin MAX ILRD subscriber registered")
                subscriber = sub
                true
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, "Failed to subscribe to AppLovin communicator")
                false
            }
        }
    }

    fun unregisterSubscriber(): Boolean {
        synchronized(this) {
            val sub = subscriber ?: run {
                DebugLogger.info(tag, "unregisterSubscriber: not registered, skipping")
                return true
            }
            return try {
                AppLovinCommunicator.getInstance(context).unsubscribe(sub, Constants.TOPIC)
                DebugLogger.info(tag, "AppLovin MAX ILRD subscriber unregistered")
                subscriber = null
                true
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, "Failed to unsubscribe from AppLovin communicator")
                false
            }
        }
    }
}
