package io.appmetrica.analytics.impl.startup

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[StartupRequiredUtils]"

internal object StartupRequiredUtils {

    private val STARTUP_TRIGGER_IDENTIFIERS = setOf(
        Constants.StartupParamsCallbackKeys.CLIDS,
        Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
        Constants.StartupParamsCallbackKeys.DEVICE_ID,
        Constants.StartupParamsCallbackKeys.GET_AD_URL,
        Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
        Constants.StartupParamsCallbackKeys.UUID
    )
    var timeProvider: TimeProvider = SystemTimeProvider()
        @VisibleForTesting get
        @VisibleForTesting set
    var clidsStateChecker = ClidsStateChecker()
        @VisibleForTesting get
        @VisibleForTesting set

    @JvmStatic
    fun isOutdated(startupState: StartupState): Boolean {
        return (
            startupState.outdated ||
                isOutdated(startupState.obtainTime + startupState.startupUpdateConfig.intervalSeconds)
            ).also {
            YLogger.info(
                TAG,
                "Is startup outdated? $it. " +
                    "Outdated field: ${startupState.outdated}, obtainTime: ${startupState.obtainTime}"
            )
        }
    }

    @JvmStatic
    fun isOutdated(nextStartupTime: Long): Boolean {
        return (timeProvider.currentTimeSeconds() > nextStartupTime).also {
            YLogger.info(
                TAG,
                "Is startup outdated? $it. Next startup time: $nextStartupTime"
            )
        }
    }

    @JvmStatic
    fun areMainIdentifiersValid(startupState: StartupState): Boolean {
        return (
            isIdentifierValid(startupState.uuid) &&
                isIdentifierValid(startupState.deviceId) &&
                isIdentifierValid(startupState.deviceIdHash)
            )
            .also {
                YLogger.info(
                    TAG,
                    "Are main identifiers valid? $it. " +
                        "Uuid: ${startupState.uuid}, " +
                        "device id: ${startupState.deviceId}, " +
                        "device id hash: ${startupState.deviceIdHash}"
                )
            }
    }

    @JvmStatic
    fun isIdentifierValid(identifier: String?): Boolean = !identifier.isNullOrEmpty()

    @JvmStatic
    fun containsIdentifiers(
        startupState: StartupState,
        identifiers: Collection<String>?,
        clientClids: Map<String, String>?,
        clidsStorageProvider: () -> ClidsInfoStorage
    ): Boolean {
        if (identifiers.isNullOrEmpty()) return true
        val result = identifiers.all {
            when (it) {
                Constants.StartupParamsCallbackKeys.DEVICE_ID ->
                    isIdentifierValid(startupState.deviceId)
                Constants.StartupParamsCallbackKeys.UUID ->
                    isIdentifierValid(startupState.uuid)
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH ->
                    isIdentifierValid(startupState.deviceIdHash)
                Constants.StartupParamsCallbackKeys.REPORT_AD_URL ->
                    isIdentifierValid(startupState.reportAdUrl)
                Constants.StartupParamsCallbackKeys.GET_AD_URL ->
                    isIdentifierValid(startupState.getAdUrl)
                Constants.StartupParamsCallbackKeys.CLIDS ->
                    clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                        clientClids,
                        startupState,
                        clidsStorageProvider()
                    )
                else -> !isOutdated(startupState)
            }
        }
        YLogger.info(
            TAG,
            "Contains identifiers $identifiers? $result." +
                "Uuid: ${startupState.uuid}, device id: ${startupState.deviceId}, " +
                "device id hash: ${startupState.deviceIdHash}," +
                "report ad url: ${startupState.reportAdUrl}, get ad url: ${startupState.getAdUrl}, " +
                "encodedClidsFromResponse: ${startupState.encodedClidsFromResponse}, " +
                "startupDidNotOverrideClids: ${startupState.startupDidNotOverrideClids}," +
                "lastClientClidsForStartupRequest: ${startupState.lastClientClidsForStartupRequest}," +
                "lastChosenForRequestClids: ${startupState.lastChosenForRequestClids}, clientClids: $clientClids," +
                "outdated: ${startupState.outdated}, obtainTime: ${startupState.obtainTime}"
        )
        return result
    }

    @JvmStatic
    fun pickIdentifiersThatShouldTriggerStartup(identifiers: Collection<String>): Collection<String> {
        return identifiers.intersect(STARTUP_TRIGGER_IDENTIFIERS).also {
            YLogger.info(TAG, "From $identifiers only $it should trigger startup")
        }
    }
}
