package io.appmetrica.analytics.impl.clids

import android.content.Context
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper
import io.appmetrica.analytics.impl.DataAwaiter

internal class ClidsDataAwaiter : DataAwaiter {

    override fun waitForData(context: Context) {
        ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
    }
}
