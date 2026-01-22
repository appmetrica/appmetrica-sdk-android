package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.Intent

internal interface ServiceWakeLockIntentProvider {

    fun getWakeLockIntent(context: Context, action: String): Intent
}
