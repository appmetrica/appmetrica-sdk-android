package io.appmetrica.analytics.impl

import android.content.Intent
import android.content.res.Configuration
import android.os.Process
import android.text.TextUtils
import io.appmetrica.analytics.impl.AppMetricaServiceLifecycle.Condition
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction
import io.appmetrica.analytics.impl.utils.collection.HashMultimap
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppMetricaServiceLifecycle : AppMetricaServiceLifecycleCallback {

    internal fun interface LifecycleObserver {
        fun onEvent(intent: Intent)
    }

    internal fun interface Condition {
        fun match(intent: Intent): Boolean
    }

    private val tag = "[AppMetricaServiceLifecycle]"

    private val boundProcesses = HashMultimap<String, Int>()
    private val connectObservers = linkedMapOf<LifecycleObserver, Condition>()
    private val disconnectObservers = linkedMapOf<LifecycleObserver, Condition>()

    private val clientPids: Collection<Int>?
        get() = boundProcesses[AppMetricaServiceAction.ACTION_CLIENT_CONNECTION]

    private val clientCounts: Int
        get() = clientPids?.size ?: 0

    override fun onCreate() {
        // Do nothing
    }

    override fun onStart(intent: Intent, startId: Int) {
        // Do nothing
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) {
        // Do nothing
    }

    override fun onBind(intent: Intent?) {
        intent?.let { handleBindOrRebind(it) }
    }

    override fun onRebind(intent: Intent?) {
        intent?.let { handleBindOrRebind(it) }
    }

    private fun handleBindOrRebind(intent: Intent) {
        val action = intent.action
        if (!TextUtils.isEmpty(action)) {
            boundProcesses.put(action, getPid(intent))
            DebugLogger.info(
                tag,
                "onBindOrRebind with action = %s. Is metrica process: %b. Current bound clients: %s",
                action,
                isMetricaProcess(getPid(intent)),
                boundProcesses
            )
        }
        notifyObservers(intent, connectObservers)
    }

    private fun notifyObservers(intent: Intent, observers: Map<LifecycleObserver, Condition>) {
        for ((key, value) in observers) {
            if (value.match(intent)) {
                key.onEvent(intent)
            }
        }
    }

    override fun onUnbind(intent: Intent?) {
        intent?.let { handleUnbind(it) }
    }

    private fun handleUnbind(intent: Intent) {
        val action = intent.action
        if (!TextUtils.isEmpty(action)) {
            boundProcesses.remove(action, getPid(intent))
            DebugLogger.info(
                tag,
                "onUnbind with action = %s. Is metrica process: %b. Current bound clients after remove: %s",
                action,
                isMetricaProcess(getPid(intent)),
                boundProcesses
            )
        }
        notifyObservers(intent, disconnectObservers)
    }

    override fun onDestroy() {
        // Do nothing
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // Do nothing
    }

    fun addFirstClientConnectObserver(lifecycleObserver: LifecycleObserver) {
        connectObservers[lifecycleObserver] = Condition { intent -> isFirstClientAction(intent) }
    }

    fun addNewClientConnectObserver(lifecycleObserver: LifecycleObserver) {
        connectObservers[lifecycleObserver] = Condition { intent -> isClientAction(intent) }
    }

    fun addAllClientDisconnectedObserver(lifecycleObserver: LifecycleObserver) {
        disconnectObservers[lifecycleObserver] =
            Condition { intent -> isClientAction(intent) && noMoreNonMetricaBoundClients() }
    }

    private fun isFirstClientAction(intent: Intent): Boolean {
        return isClientAction(intent) && hasSingleBoundClient()
    }

    private fun isClientAction(action: String?): Boolean {
        return AppMetricaServiceAction.ACTION_CLIENT_CONNECTION == action
    }

    private fun isClientAction(intent: Intent): Boolean {
        return isClientAction(intent.action)
    }

    private fun hasSingleBoundClient(): Boolean {
        return clientCounts == 1
    }

    private fun noMoreNonMetricaBoundClients(): Boolean {
        return clientCounts == 0
    }

    private fun isMetricaProcess(pid: Int): Boolean {
        return pid == Process.myPid()
    }

    private fun getPid(intent: Intent): Int {
        var pid = -1
        val intentData = intent.data
        if (intentData != null && intentData.path == "/" + ServiceUtils.PATH_CLIENT) {
            try {
                pid = intentData.getQueryParameter(ServiceUtils.PARAMETER_PID)!!
                    .toInt()
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, e.message)
            }
        }
        return pid
    }
}
