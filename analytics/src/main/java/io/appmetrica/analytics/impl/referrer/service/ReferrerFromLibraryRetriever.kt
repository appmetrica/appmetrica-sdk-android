package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReferrerFromLibraryRetriever internal constructor(
    context: Context,
    private val executor: ICommonExecutor
) : IReferrerRetriever {

    private val tag = "[ReferrerFromLibraryRetriever]"

    private val client = InstallReferrerClient.newBuilder(context).build()

    @Throws(Throwable::class)
    override fun retrieveReferrer(referrerListener: ReferrerReceivedListener) {
        DebugLogger.info(tag, "try to retrieve referrer via Google Play referrer library")
        val listener: InstallReferrerStateListener = object : InstallReferrerStateListener {
            @MainThread
            override fun onInstallReferrerSetupFinished(i: Int) {
                if (i == InstallReferrerClient.InstallReferrerResponse.OK) {
                    executor.execute {
                        try {
                            val referrerDetails = client.installReferrer
                            val referrerInfo = ReferrerInfo(
                                referrerDetails.installReferrer,
                                referrerDetails.referrerClickTimestampSeconds,
                                referrerDetails.installBeginTimestampSeconds,
                                ReferrerInfo.Source.GP
                            )
                            referrerListener.onReferrerReceived(referrerInfo)
                        } catch (e: Throwable) {
                            DebugLogger.error(tag, e)
                            referrerListener.onReferrerRetrieveError(e)
                        } finally {
                            try {
                                client.endConnection()
                            } catch (ex: Throwable) {
                                DebugLogger.error(tag, ex)
                            }
                        }
                    }
                } else {
                    notifyListenerOnError(
                        referrerListener,
                        IllegalStateException("Referrer check failed with error $i")
                    )
                }
            }

            @MainThread
            override fun onInstallReferrerServiceDisconnected() {
                // do nothing
            }
        }
        client.startConnection(listener)
    }

    @AnyThread
    private fun notifyListenerOnError(
        referrerListener: ReferrerReceivedListener,
        ex: Throwable
    ) {
        executor.execute { referrerListener.onReferrerRetrieveError(ex) }
    }
}
