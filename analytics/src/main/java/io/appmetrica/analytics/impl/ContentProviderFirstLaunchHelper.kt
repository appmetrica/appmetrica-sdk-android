package io.appmetrica.analytics.impl

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import io.appmetrica.analytics.internal.PreloadInfoContentProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal object ContentProviderFirstLaunchHelper {

    private const val TAG = "[ContentProviderFirstLaunchHelper]"

    @Volatile
    private var countDownLatch: CountDownLatch? = null
    @Volatile
    private var contentProvider: PreloadInfoContentProvider? = null

    @JvmStatic
    fun onCreate(contentProvider: PreloadInfoContentProvider) {
        DebugLogger.info(TAG, "On Create")
        countDownLatch = CountDownLatch(1)
        this.contentProvider = contentProvider
    }

    @JvmStatic
    fun onInsertFinished() {
        DebugLogger.info(TAG, "Insert finished")
        countDownLatch?.countDown()
    }

    @JvmStatic
    fun awaitContentProviderWarmUp(context: Context) {
        countDownLatch?.let {
            // Wait for possible updates from insert.
            // When clids are set by content provider it provokes AppMetrica activation in Application#onCreate.
            // We want first startup to use set clids
            // so we wait in case insert finishes later than we start choosing clids for startup.
            it.await(1, TimeUnit.SECONDS)
            try {
                context.packageManager.setComponentEnabledSetting(
                    ComponentName(context, PreloadInfoContentProvider::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                contentProvider?.disable()
                DebugLogger.info(TAG, "Provider disabled successfully")
            } catch (ex: Throwable) {
                DebugLogger.error(TAG, ex)
            }
            countDownLatch = null
        }
    }
}
