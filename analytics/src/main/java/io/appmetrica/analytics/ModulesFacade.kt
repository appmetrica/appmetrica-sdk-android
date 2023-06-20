package io.appmetrica.analytics

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.proxy.ModulesProxy

object ModulesFacade {

    private val tag = "[ModulesFacade]"

    private var sProxy = ModulesProxy(
        ClientServiceLocator.getInstance().apiProxyExecutor
    )

    @JvmStatic
    fun reportEvent(
        moduleEvent: ModuleEvent
    ) {
        YLogger.info(tag, "reportEvent: $moduleEvent")
        sProxy.reportEvent(moduleEvent)
    }

    @JvmStatic
    fun setSessionExtra(key: String, value: ByteArray?) {
        YLogger.info(tag, "setSessionExtra with key = `$key` and value size: ${value?.size}")
        sProxy.setSessionExtra(key, value)
    }

    @JvmStatic
    fun isActivatedForApp(): Boolean {
        return sProxy.isActivatedForApp()
    }

    @JvmStatic
    fun sendEventsBuffer() {
        sProxy.sendEventsBuffer()
    }

    @JvmStatic
    fun getModuleReporter(context: Context, apiKey: String): IModuleReporter {
        return sProxy.getReporter(context, apiKey)
    }

    @VisibleForTesting
    fun setProxy(proxy: ModulesProxy) {
        sProxy = proxy
    }
}
