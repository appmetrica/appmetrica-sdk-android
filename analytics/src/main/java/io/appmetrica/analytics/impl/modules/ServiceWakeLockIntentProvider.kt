package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.Intent

interface ServiceWakeLockIntentProvider {

    fun getWakeLockIntent(context: Context, action: String): Intent
}
