package io.appmetrica.analytics.impl.proxy.synchronous

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.Revenue
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable
import io.appmetrica.analytics.impl.crash.client.AllThreads
import io.appmetrica.analytics.impl.crash.client.UnhandledException
import io.appmetrica.analytics.profile.UserProfile

class ReporterSynchronousStageExecutor {

    fun putAppEnvironmentValue(key: String, value: String?) {}

    fun clearAppEnvironment() {}

    fun sendEventsBuffer() {}

    fun reportEvent(eventName: String) {}

    fun reportEvent(eventName: String, jsonValue: String?) {}

    fun reportEvent(eventName: String, attributes: Map<String?, Any?>?) {}

    fun reportEvent(moduleEvent: ModuleEvent) {}

    fun reportError(message: String, error: Throwable?): Throwable {
        return error ?: AppMetricaThrowable().apply {
            fillInStackTrace()
        }
    }

    fun reportError(identifier: String, message: String?, error: Throwable?) {}

    fun reportUnhandledException(exception: Throwable) {}

    fun resumeSession() {}

    fun pauseSession() {}

    fun setUserProfileID(profileID: String?) {}

    fun reportUserProfile(profile: UserProfile) {}

    fun reportRevenue(revenue: Revenue) {}

    fun reportAdRevenue(adRevenue: AdRevenue) {}

    fun reportECommerce(event: ECommerceEvent) {}

    fun activate(config: ReporterConfig) {}

    fun setDataSendingEnabled(enabled: Boolean) {}

    fun reportUnhandledException(unhandledException: UnhandledException) {}

    fun reportAnr(allThreads: AllThreads) {}

    fun setSessionExtra(key: String, value: ByteArray?) {}
}
