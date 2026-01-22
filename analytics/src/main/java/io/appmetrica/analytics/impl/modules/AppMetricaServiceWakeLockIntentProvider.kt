package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.Intent
import io.appmetrica.analytics.internal.AppMetricaService

internal class AppMetricaServiceWakeLockIntentProvider : ServiceWakeLockIntentProvider {

    override fun getWakeLockIntent(context: Context, action: String): Intent =
        Intent(context, AppMetricaService::class.java).apply {
            this.action = action
        }
}
