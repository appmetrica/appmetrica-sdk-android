package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.reflection.Constants
import io.appmetrica.analytics.impl.id.reflection.ReflectionAdvIdExtractor
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

internal class AdvertisingIdGetter internal constructor(
    private val context: Context,
    private val executor: ICommonExecutor,
    startupState: StartupState,
) : IAdvertisingIdGetter {

    private val tag = "[AdvertisingIdGetter]"

    private val trackingDisabledByApi = "advertising identifiers collecting is forbidden by client configuration"
    private val featureDisabled = "advertising identifiers collecting is forbidden by startup"
    private val unknownProblem = "advertising identifiers collecting is forbidden by unknown reason"

    private val googleExtractor: AdvIdExtractor =
        AdvIdExtractorWrapper(ReflectionAdvIdExtractor(Constants.Providers.GOOGLE))
    private val huaweiExtractor: AdvIdExtractor =
        AdvIdExtractorWrapper(ReflectionAdvIdExtractor(Constants.Providers.HUAWEI))
    private val yandexExtractor: AdvIdExtractor =
        AdvIdExtractorWrapper(ReflectionAdvIdExtractor(Constants.Providers.YANDEX))

    private lateinit var blockingRefreshTask: FutureTask<Void>
    private val backgroundRefreshTask: FutureTask<Void> by lazy {
        FutureTask<Void> {
            DebugLogger.info(tag, "Start async refresh task")
            updateAdvIdentifiers()
            null
        }
    }
    private val backgroundRefreshIntervalSeconds = 90L
    private val controller = AdvIdGetterController(startupState)

    @Volatile
    private var advertisingIdsHolder = AdvertisingIdsHolder()
    private var canTrackIdentifiers: AdvIdGetterController.CanTrackIdentifiers =
        AdvIdGetterController.CanTrackIdentifiers(
            AdvIdGetterController.State.UNKNOWN,
            AdvIdGetterController.State.UNKNOWN,
            AdvIdGetterController.State.UNKNOWN
        )

    private var hasInitialStateFromClient = false

    @Synchronized
    override fun init() {
        if (!this::blockingRefreshTask.isInitialized) {
            canTrackIdentifiers = controller.canTrackIdentifiers()
            blockingRefreshTask = FutureTask<Void> {
                DebugLogger.info(tag, "init advertising identifiers")
                advertisingIdsHolder = AdvertisingIdsHolder(
                    extractGaidIfAllowed(canTrackIdentifiers.canTrackGaid),
                    extractHoaidIfAllowed(canTrackIdentifiers.canTrackHoaid),
                    extractYandexAdvIdIfAllowed(canTrackIdentifiers.canTrackYandexAdvId, NoRetriesStrategy())
                )
                DebugLogger.info(tag, "Initial advertising identifiers: $advertisingIdsHolder")
                scheduleBackgroundRefreshTask()
                null
            }
            executor.execute(blockingRefreshTask)
        }
    }

    @Synchronized
    override fun onStartupStateChanged(startupState: StartupState) {
        DebugLogger.info(
            tag,
            "onStartupStateChanged: canCollectGaid = ${startupState.collectingFlags.googleAid}; " +
                "canCollectHuaweiOaid = ${startupState.collectingFlags.huaweiOaid}"
        )
        controller.updateStartupState(startupState)
        refreshIdentifiers()
    }

    @Synchronized
    override fun setInitialStateFromClientConfigIfNotDefined(enabled: Boolean) {
        if (!hasInitialStateFromClient) {
            DebugLogger.info(tag, "setInitialStateFromClientConfigIfNotDefined to $enabled")
            updateStateFromClientConfig(enabled)
        }
    }

    @Synchronized
    override fun updateStateFromClientConfig(enabled: Boolean) {
        DebugLogger.info(tag, "updateStateFromClientConfig to $enabled")
        hasInitialStateFromClient = true
        controller.updateStateFromClientConfig(enabled)
        refreshIdentifiers()
    }

    override val identifiers: AdvertisingIdsHolder
        @Synchronized
        get() {
            getValue(blockingRefreshTask)
            return advertisingIdsHolder
        }

    private fun refreshIdentifiers(): FutureTask<Void> {
        val canTrackIdentifiers = controller.canTrackIdentifiers()
        if (canTrackIdentifiers != this.canTrackIdentifiers) {
            DebugLogger.info(
                tag,
                "Current options (${this.canTrackIdentifiers}) != incoming ($canTrackIdentifiers). " +
                    "Prepare refresh task..."
            )
            removeBackgroundRefreshTask()
            this.canTrackIdentifiers = canTrackIdentifiers
            blockingRefreshTask = FutureTask<Void> {
                DebugLogger.info(tag, "Extract identifiers from system with blocking task")
                updateAdvIdentifiers()
                null
            }
        } else {
            DebugLogger.info(tag, "Advertising identifiers can track didn't changed. Ignore refresh identifiers")
        }
        executor.execute(blockingRefreshTask)
        return blockingRefreshTask
    }

    private fun updateAdvIdentifiers() {
        val result = AdvertisingIdsHolder(
            mergeIdentifierData(
                extractGaidIfAllowed(canTrackIdentifiers.canTrackGaid),
                advertisingIdsHolder.google
            ),
            mergeIdentifierData(
                extractHoaidIfAllowed(canTrackIdentifiers.canTrackHoaid),
                advertisingIdsHolder.huawei
            ),
            mergeIdentifierData(
                extractYandexAdvIdIfAllowed(
                    canTrackIdentifiers.canTrackYandexAdvId,
                    TimesBasedRetryStrategy(3, 500)
                ),
                advertisingIdsHolder.yandex
            )
        )
        DebugLogger.info(tag, "Adv identifiers updated from $advertisingIdsHolder -> $result")
        advertisingIdsHolder = result
        scheduleBackgroundRefreshTask()
    }

    private fun scheduleBackgroundRefreshTask() {
        executor.executeDelayed(backgroundRefreshTask, backgroundRefreshIntervalSeconds, TimeUnit.SECONDS)
    }

    private fun removeBackgroundRefreshTask() {
        executor.remove(backgroundRefreshTask)
    }

    private fun getValue(future: FutureTask<Void>) {
        try {
            future.get()
        } catch (e: InterruptedException) {
            DebugLogger.error(tag, "can't get adv_id. Error: %s", e.message)
        } catch (e: ExecutionException) {
            DebugLogger.error(tag, "can't get adv_id. Error: %s", e.message)
        }
    }

    private fun mergeIdentifierData(
        newData: AdTrackingInfoResult,
        cachedData: AdTrackingInfoResult
    ): AdTrackingInfoResult {
        return if (newData.mStatus == IdentifierStatus.UNKNOWN) {
            AdTrackingInfoResult(
                cachedData.mAdTrackingInfo,
                newData.mStatus,
                newData.mErrorExplanation
            )
        } else {
            newData
        }
    }

    private fun extractGaidIfAllowed(state: AdvIdGetterController.State): AdTrackingInfoResult {
        return extractWithState(state) { googleExtractor.extractAdTrackingInfo(context) }
    }

    private fun extractHoaidIfAllowed(state: AdvIdGetterController.State): AdTrackingInfoResult {
        return extractWithState(state) { huaweiExtractor.extractAdTrackingInfo(context) }
    }

    private fun extractYandexAdvIdIfAllowed(
        state: AdvIdGetterController.State,
        retryStrategy: RetryStrategy
    ): AdTrackingInfoResult {
        return extractWithState(state) { yandexExtractor.extractAdTrackingInfo(context, retryStrategy) }
    }

    private fun extractWithState(
        state: AdvIdGetterController.State,
        extractor: () -> AdTrackingInfoResult
    ): AdTrackingInfoResult {
        return when (state) {
            AdvIdGetterController.State.ALLOWED -> extractor()
            AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG -> errorForForbiddenByClientConfig()
            AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG -> errorForForbiddenByRemoteConfig()
            AdvIdGetterController.State.UNKNOWN -> unknownError()
        }
    }

    private fun errorForForbiddenByClientConfig() = AdTrackingInfoResult(
        null,
        IdentifierStatus.FORBIDDEN_BY_CLIENT_CONFIG,
        trackingDisabledByApi
    )

    private fun errorForForbiddenByRemoteConfig() = AdTrackingInfoResult(
        null,
        IdentifierStatus.FEATURE_DISABLED,
        featureDisabled
    )

    private fun unknownError() = AdTrackingInfoResult(
        null,
        IdentifierStatus.UNKNOWN,
        unknownProblem
    )
}
