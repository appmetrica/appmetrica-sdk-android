package io.appmetrica.analytics.impl.referrer.service.provider.google

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class GooglePlayReferrerLibrary(private val executor: ICommonExecutor) {
    private val tag = "[GooglePlayReferrerLibrary]"

    fun requestReferrer(context: Context, listener: ReferrerListener) {
        try {
            DebugLogger.info(tag, "Try to get referrer via Google Play referrer library")
            val client = InstallReferrerClient.newBuilder(context).build()
            client.startConnection(LibraryReferrerListener(client, listener))
        } catch (e: Throwable) {
            val message = "Failed to get referrer from Google Play referrer library"
            DebugLogger.warning(tag, "$message: ${e.message}")
            AppMetricaSelfReportFacade.getReporter().reportError(message, e)
            listener.onResult(ReferrerResult.Failure(message, e))
        }
    }

    private inner class LibraryReferrerListener(
        private val client: InstallReferrerClient,
        private val listener: ReferrerListener,
    ) : InstallReferrerStateListener {
        private val tag = "[GooglePlayReferrerLibrary.LibraryReferrerListener]"

        @MainThread
        override fun onInstallReferrerSetupFinished(responseCode: Int) {
            DebugLogger.info(tag, "onInstallReferrerSetupFinished: $responseCode")
            executor.execute {
                val result = runCatching {
                    if (responseCode != InstallReferrerResponse.OK) {
                        reportResponseErrorOnSelfReporter(responseCode)
                        val message = "The connection returned an error code $responseCode"
                        DebugLogger.warning(tag, message)
                        return@runCatching ReferrerResult.Failure(message)
                    }

                    val referrer = getReferrer()
                    if (referrer == null) {
                        val message = "Referrer is null"
                        DebugLogger.warning(tag, message)
                        return@runCatching ReferrerResult.Failure(message)
                    }

                    DebugLogger.info(tag, "Successful get referrer from Google Play referrer library: $referrer")
                    ReferrerResult.Success(referrer)
                }.getOrElse { error ->
                    val message = "Failed to get referrer via Google Play referrer library"
                    DebugLogger.warning(tag, "$message: ${error.message}")
                    AppMetricaSelfReportFacade.getReporter().reportError(message, error)
                    ReferrerResult.Failure(message, error)
                }

                client.closeConnectionSafely()
                listener.onResult(result)
            }
        }

        /**
         * https://developer.android.com/reference/com/android/installreferrer/api/InstallReferrerStateListener
         * Called to notify that connection to install referrer service was lost.
         *
         * Note: This does not remove install referrer service connection itself - this binding to the service will
         * remain active, and you will receive a call to onInstallReferrerSetupFinished(int) when install referrer
         * service is next running and setup is complete.
         */
        @MainThread
        override fun onInstallReferrerServiceDisconnected() {
            DebugLogger.warning(tag, "onInstallReferrerServiceDisconnected")
        }

        @WorkerThread
        private fun getReferrer(): ReferrerInfo? {
            val referrer = client.installReferrer.also(::logReferrer) ?: return null
            if (referrer.installReferrer.isNullOrBlank()) return null
            return ReferrerInfo(
                referrer.installReferrer,
                referrer.referrerClickTimestampSeconds,
                referrer.installBeginTimestampSeconds,
                ReferrerInfo.Source.GP,
            )
        }

        @WorkerThread
        private fun logReferrer(referrer: ReferrerDetails?) {
            DebugLogger.info(
                tag,
                "Google Play referrer library return referrer: " + referrer?.let {
                    buildString {
                        append("ReferrerDetails(")
                        append("installReferrer=")
                        append(it.installReferrer)
                        append(", referrerClickTimestampSeconds=")
                        append(it.referrerClickTimestampSeconds)
                        append(", installBeginTimestampSeconds=")
                        append(it.installBeginTimestampSeconds)
                        append(")")
                    }
                }
            )
        }

        @WorkerThread
        private fun reportResponseErrorOnSelfReporter(responseCode: Int) {
            if (responseCode == InstallReferrerResponse.DEVELOPER_ERROR) {
                AppMetricaSelfReportFacade.getReporter().reportError(
                    "Failed to get referrer via Google Play referrer library",
                    IllegalStateException("Developer error"),
                )
            } else if (responseCode == InstallReferrerResponse.PERMISSION_ERROR) {
                AppMetricaSelfReportFacade.getReporter().reportError(
                    "Failed to get referrer via Google Play referrer library",
                    IllegalStateException("Permission error"),
                )
            }
        }

        @AnyThread
        private fun InstallReferrerClient.closeConnectionSafely() {
            try {
                endConnection()
            } catch (e: Throwable) {
                DebugLogger.error(tag, e, "Failed to close connection to Google Play referrer library")
            }
        }
    }
}
