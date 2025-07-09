package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.id.reflection.Constants
import io.appmetrica.analytics.impl.id.reflection.ReflectionAdvIdProvider
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

internal class AdvertisingIdGetter internal constructor(
    private val context: Context,
    private val executor: ICommonExecutor,
    startupState: StartupState,
) : IAdvertisingIdGetter {

    private val tag = "[AdvertisingIdGetter]"

    private val trackingDisabledByApi = "advertising identifiers collecting is forbidden by client configuration"
    private val featureDisabled = "advertising identifiers collecting is forbidden by startup"
    private val unknownProblem = "advertising identifiers collecting is forbidden by unknown reason"

    private val googleProvider: AdvIdProvider =
        AdvIdProviderWrapper(ReflectionAdvIdProvider(Constants.Providers.GOOGLE))
    private val huaweiProvider: AdvIdProvider =
        AdvIdProviderWrapper(ReflectionAdvIdProvider(Constants.Providers.HUAWEI))
    private val yandexProvider: AdvIdProvider =
        AdvIdProviderWrapper(ReflectionAdvIdProvider(Constants.Providers.YANDEX))

    private lateinit var refresh: FutureTask<Void>
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
        if (!this::refresh.isInitialized) {
            canTrackIdentifiers = controller.canTrackIdentifiers()
            refresh = FutureTask<Void> {
                DebugLogger.info(tag, "init advertising identifiers")
                advertisingIdsHolder = AdvertisingIdsHolder(
                    extractGaidIfAllowed(canTrackIdentifiers.canTrackGaid),
                    extractHoaidIfAllowed(canTrackIdentifiers.canTrackHoaid),
                    extractYandexAdvIdIfAllowed(canTrackIdentifiers.canTrackYandexAdvId, NoRetriesStrategy())
                )
                DebugLogger.info(tag, "Initial advertising identifiers: $advertisingIdsHolder")
                null
            }
            executor.execute(refresh)
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

    @Synchronized
    override fun getIdentifiers(context: Context): AdvertisingIdsHolder {
        return identifiers
    }

    override val identifiers: AdvertisingIdsHolder
        @Synchronized
        get() {
            getValue(refresh)
            return advertisingIdsHolder
        }

    override val identifiersForced: AdvertisingIdsHolder
        @Synchronized
        get() = getIdentifiersForced(NoRetriesStrategy())

    @Synchronized
    override fun getIdentifiersForced(yandexRetryStrategy: RetryStrategy): AdvertisingIdsHolder {
        getValue(refreshIdentifiers(yandexRetryStrategy, true))
        return advertisingIdsHolder
    }

    private fun refreshIdentifiers(): FutureTask<Void> {
        return refreshIdentifiers(NoRetriesStrategy(), false)
    }

    private fun refreshIdentifiers(yandexRetryStrategy: RetryStrategy, force: Boolean): FutureTask<Void> {
        val canTrackIdentifiers = controller.canTrackIdentifiers()
        refresh = FutureTask<Void> {
            if (force || canTrackIdentifiers != this.canTrackIdentifiers) {
                DebugLogger.info(tag, "get advertising identifiers forced")
                val advertisingIdsHolderFixed = advertisingIdsHolder
                advertisingIdsHolder = AdvertisingIdsHolder(
                    mergeIdentifierData(
                        extractGaidIfAllowed(canTrackIdentifiers.canTrackGaid),
                        advertisingIdsHolderFixed.google
                    ),
                    mergeIdentifierData(
                        extractHoaidIfAllowed(canTrackIdentifiers.canTrackHoaid),
                        advertisingIdsHolderFixed.huawei
                    ),
                    mergeIdentifierData(
                        extractYandexAdvIdIfAllowed(canTrackIdentifiers.canTrackYandexAdvId, yandexRetryStrategy),
                        advertisingIdsHolderFixed.yandex
                    )
                )
                DebugLogger.info(
                    tag,
                    "Update advIdentifiers from $advertisingIdsHolderFixed to $advertisingIdsHolder"
                )
            } else {
                DebugLogger.info(tag, "Ignore refresh as canTrackIdentifiers didn't change")
            }
            null
        }
        executor.execute(refresh)
        return refresh
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
        return extractWithState(state) { googleProvider.getAdTrackingInfo(context) }
    }

    private fun extractHoaidIfAllowed(state: AdvIdGetterController.State): AdTrackingInfoResult {
        return extractWithState(state) { huaweiProvider.getAdTrackingInfo(context) }
    }

    private fun extractYandexAdvIdIfAllowed(
        state: AdvIdGetterController.State,
        retryStrategy: RetryStrategy
    ): AdTrackingInfoResult {
        return extractWithState(state) { yandexProvider.getAdTrackingInfo(context, retryStrategy) }
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
