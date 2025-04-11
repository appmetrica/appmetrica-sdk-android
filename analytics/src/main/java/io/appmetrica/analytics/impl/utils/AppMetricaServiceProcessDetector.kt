package io.appmetrica.analytics.impl.utils

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.internal.AppMetricaService

class AppMetricaServiceProcessDetector {

    fun isMainProcess(context: Context): Boolean {
        return processName(context)?.let { !it.contains(":") } ?: true
    }

    fun isNonMainProcess(context: Context): Boolean = !isMainProcess(context)

    fun processName(context: Context): String? {
        return PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java)?.processName
    }
}
