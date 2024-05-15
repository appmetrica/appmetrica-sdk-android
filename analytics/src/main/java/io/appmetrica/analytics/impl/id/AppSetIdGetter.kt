package io.appmetrica.analytics.impl.id

import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.appsetid.internal.AppSetIdListener
import io.appmetrica.analytics.appsetid.internal.AppSetIdRetriever
import io.appmetrica.analytics.appsetid.internal.IAppSetIdRetriever
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.internal.DebugLogger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val APP_SET_ID_LIB_CLASS = "com.google.android.gms.appset.AppSet"
private const val TAG = "[AppSetIdGetter]"

internal class AppSetIdGetter @VisibleForTesting constructor(
    private val context: Context,
    private val appSetIdRetriever: IAppSetIdRetriever
) : AppSetIdProvider {

    constructor(context: Context) : this(context, createAppSetIdRetriever())

    @Volatile
    private var appSetId: AppSetId? = null
    private var countDownLatch = CountDownLatch(1)
    private val timeoutSeconds = 20L

    private val appSetIdListener: AppSetIdListener = object : AppSetIdListener {
        @MainThread
        override fun onAppSetIdRetrieved(id: String?, scope: AppSetIdScope) {
            DebugLogger.info(TAG, "Received id: $id, scope: $scope")
            appSetId = AppSetId(id, scope)
            countDownLatch.countDown()
        }

        @MainThread
        override fun onFailure(ex: Throwable?) {
            DebugLogger.error(TAG, ex)
            countDownLatch.countDown()
        }
    }

    @Synchronized
    @WorkerThread
    override fun getAppSetId(): AppSetId {
        DebugLogger.info(TAG, "Retrieve app set id. Current: $appSetId")
        if (appSetId == null) {
            try {
                countDownLatch = CountDownLatch(1)
                appSetIdRetriever.retrieveAppSetId(context, appSetIdListener)
                countDownLatch.await(timeoutSeconds, TimeUnit.SECONDS)
            } catch (ex: Throwable) {
                DebugLogger.error(TAG, ex)
            }
        }
        return appSetId ?: AppSetId(null, AppSetIdScope.UNKNOWN).also { appSetId = it }
    }
}

private fun createAppSetIdRetriever(): IAppSetIdRetriever {
    return if (ReflectionUtils.detectClassExists(APP_SET_ID_LIB_CLASS)) {
        AppSetIdRetriever()
    } else {
        DummyAppSetIdRetriever()
    }
}
