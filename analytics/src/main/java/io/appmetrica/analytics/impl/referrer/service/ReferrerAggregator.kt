package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class ReferrerAggregator @VisibleForTesting internal constructor(
    private val referrerHolder: ReferrerHolder,
    private val googleReferrerRetriever: ReferrerRetrieverWrapper,
    private val huaweiReferrerRetriever: HuaweiReferrerRetriever,
    private val referrerValidityChecker: ReferrerValidityChecker,
) {

    private val tag = "[ReferrerAggregator]"

    private interface BadReferrerHandler {
        @WorkerThread
        fun onBadReferrer()
    }

    private val badHuaweiReferrerHandler: BadReferrerHandler by lazy {
        object : BadReferrerHandler {
            @WorkerThread
            override fun onBadReferrer() {
                chooseReferrerAnyway()
            }
        }
    }
    private val badGoogleReferrerHandler: BadReferrerHandler by lazy {
        object : BadReferrerHandler {
            @WorkerThread
            override fun onBadReferrer() {
                DebugLogger.info(tag, "Retrieve huawei referrer as google does not match package installer")
                huaweiReferrerRetriever.retrieveReferrer(object : ReferrerReceivedListener {
                    @WorkerThread
                    override fun onReferrerReceived(referrerInfo: ReferrerInfo?) {
                        onSomeReferrerChecked(referrerInfo, badHuaweiReferrerHandler)
                    }

                    @WorkerThread
                    override fun onReferrerRetrieveError(exception: Throwable) {
                        DebugLogger.info(tag, "onReferrerRetrieveError for huawei: %s", exception)
                        onSomeReferrerChecked(null, badHuaweiReferrerHandler)
                    }
                })
            }
        }
    }
    private val googleListener: ReferrerReceivedListener by lazy {
        object : ReferrerReceivedListener {
            @WorkerThread
            override fun onReferrerReceived(referrerInfo: ReferrerInfo?) {
                onSomeReferrerChecked(referrerInfo, badGoogleReferrerHandler)
            }

            @WorkerThread
            override fun onReferrerRetrieveError(exception: Throwable) {
                DebugLogger.info(tag, "onReferrerRetrieveError for google: %s", exception)
                onSomeReferrerChecked(null, badGoogleReferrerHandler)
            }
        }
    }
    private val cachedReferrers: MutableList<ReferrerInfo?> = mutableListOf()

    constructor(context: Context, referrerHolder: ReferrerHolder) : this(
        referrerHolder,
        ReferrerRetrieverWrapper(context, GlobalServiceLocator.getInstance().serviceExecutorProvider.defaultExecutor),
        HuaweiReferrerRetriever(context),
        ReferrerValidityChecker(context)
    )

    fun retrieveReferrer() {
        DebugLogger.info(tag, "retrieveReferrer")
        googleReferrerRetriever.retrieveReferrer(googleListener)
    }

    @WorkerThread
    private fun onSomeReferrerChecked(
        referrer: ReferrerInfo?,
        badReferrerHandler: BadReferrerHandler
    ) {
        cachedReferrers.add(referrer)
        if (referrerValidityChecker.doesInstallerMatchReferrer(referrer)) {
            DebugLogger.info(tag, "Choosing referrer %s as it matches package installer", referrer)
            onServicesReferrerChosen(referrer)
        } else {
            badReferrerHandler.onBadReferrer()
        }
    }

    private fun chooseReferrerAnyway() {
        DebugLogger.info(tag, "Choosing referrer between candidates: %s", cachedReferrers)
        val validReferrersList: List<ReferrerInfo> = cachedReferrers
            .filter { referrerValidityChecker.hasReferrer(it) }
            // this is unnecessary (hasReferrer contains the not null check) but kotlin won't let me do it otherwise
            .filterNotNull()
        val chosenReferrer = referrerValidityChecker.chooseReferrerFromValid(validReferrersList)
        DebugLogger.info(tag, "Chose referrer: $chosenReferrer")
        onServicesReferrerChosen(chosenReferrer)
    }

    @WorkerThread
    private fun onServicesReferrerChosen(referrer: ReferrerInfo?) {
        DebugLogger.info(tag, "onServicesReferrerChosen: %s", referrer)
        referrerHolder.storeReferrer(referrer)
    }
}
