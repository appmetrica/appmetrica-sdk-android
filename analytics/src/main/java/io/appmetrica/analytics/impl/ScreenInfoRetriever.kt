package io.appmetrica.analytics.impl

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo
import io.appmetrica.analytics.coreutils.internal.AndroidUtils.isApiAchieved
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.lang.ref.WeakReference

class ScreenInfoRetriever : ActivityAppearedListener.Listener {

    private val tag = "[ScreenInfoRetriever]"

    private var storage: PreferencesClientDbStorage? = null
    private var screenInfo: ScreenInfo? = null
    private var checkedByDeprecated = false
    private var loadedFromStorage = false

    private val screenInfoExtractor = ScreenInfoExtractor()
    private var activityHolder = WeakReference<Activity?>(null)

    @WorkerThread
    @Synchronized
    fun retrieveScreenInfo(context: Context): ScreenInfo? {
        loadFromStorage(context)
        tryToUpdateScreenInfo(activityHolder.get())
        if (screenInfo == null) {
            if (isApiAchieved(Build.VERSION_CODES.R)) {
                DebugLogger.info(
                    tag,
                    "Screen info not found. Maybe update by deprecated method. checkedByDeprecated = %b",
                    checkedByDeprecated
                )
                if (!checkedByDeprecated) {
                    tryToUpdateScreenInfo(context)
                    checkedByDeprecated = true
                    getOrCreateStorage(context).markScreenSizeCheckedByDeprecated()
                }
            } else {
                tryToUpdateScreenInfo(context)
            }
        }
        return screenInfo
    }

    @WorkerThread
    @Synchronized
    override fun onActivityAppeared(activity: Activity) {
        DebugLogger.info(tag, "Activity appeared: %s", activity)
        activityHolder = WeakReference(activity)
        loadFromStorage(activity)
        if (screenInfo == null) {
            tryToUpdateScreenInfo(activity)
        }
    }

    private fun loadFromStorage(context: Context) {
        if (loadedFromStorage) {
            return
        }
        screenInfo = getOrCreateStorage(context).screenInfo
        checkedByDeprecated = getOrCreateStorage(context).isScreenSizeCheckedByDeprecated
        DebugLogger.info(
            tag,
            "Loaded from storage screen info: %s, is screen size checked by deprecated %s",
            screenInfo,
            checkedByDeprecated
        )
        loadedFromStorage = true
    }

    private fun tryToUpdateScreenInfo(context: Context?) {
        DebugLogger.info(tag, "try to update screen info for context: %s", context)
        if (context != null) {
            val newScreenInfo = screenInfoExtractor.extractScreenInfo(context)
            DebugLogger.info(
                tag,
                "Extracted screen info: %s, old screen info: %s",
                newScreenInfo,
                screenInfo
            )
            if (newScreenInfo != null && newScreenInfo != screenInfo) {
                screenInfo = newScreenInfo
                getOrCreateStorage(context).saveScreenInfo(screenInfo)
            }
        }
    }

    private fun getOrCreateStorage(context: Context): PreferencesClientDbStorage {
        if (storage == null) {
            storage = PreferencesClientDbStorage(
                ClientServiceLocator.getInstance().getStorageFactory(context).getClientDbHelper(context)
            )
        }
        return storage!!
    }

    @VisibleForTesting
    fun setStorage(storage: PreferencesClientDbStorage) {
        this.storage = storage
    }
}
