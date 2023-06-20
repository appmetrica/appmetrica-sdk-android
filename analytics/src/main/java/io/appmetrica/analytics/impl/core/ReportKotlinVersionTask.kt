package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import java.util.concurrent.TimeUnit

private const val TAG = "[ReportKotlinVersionTask]"

class ReportKotlinVersionTask : Runnable {

    override fun run() {
        val timePassedChecker = TimePassedChecker()
        val timeProvider = SystemTimeProvider()
        val preferences = GlobalServiceLocator.getInstance().servicePreferences
        val shouldSend = timePassedChecker.didTimePassMillis(
            preferences.lastKotlinVersionSendTime(),
            TimeUnit.DAYS.toMillis(1),
            TAG
        )
        if (shouldSend) {
            val version = KotlinVersion.CURRENT
            val eventValue = mapOf(
                "major" to version.major,
                "minor" to version.minor,
                "patch" to version.patch,
                "version" to "${version.major}.${version.minor}.${version.patch}"
            )
            AppMetricaSelfReportFacade.getReporter().reportEvent("kotlin_version", eventValue)
            preferences.putLastKotlinVersionSendTime(timeProvider.currentTimeMillis()).commit()
        }
    }
}
