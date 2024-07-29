package io.appmetrica.analytics.impl.startup

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.IBaseComponent
import io.appmetrica.analytics.impl.network.NetworkTaskFactory.createStartupTask
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.StartupStateModel.StartupStateBuilder
import io.appmetrica.analytics.impl.startup.parsing.StartupParser
import io.appmetrica.analytics.impl.startup.parsing.StartupResult
import io.appmetrica.analytics.impl.utils.ServerTime
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networktasks.internal.NetworkTask

internal class StartupUnit(
    private val startupUnitComponents: StartupUnitComponents
) : IBaseComponent, IStartupUnit {

    private val tag = "[StartupUnit]"

    @Volatile
    private var currentTask: NetworkTask? = null

    val startupState: StartupState
        get() = startupUnitComponents.startupConfigurationHolder.startupState

    val requestConfig: StartupRequestConfig
        get() = startupUnitComponents.startupConfigurationHolder.get()

    fun init() {
        DebugLogger.info(tag, "Init startup unit for componentId = ${startupUnitComponents.componentId}")
        val startupState = startupUnitComponents.startupConfigurationHolder.startupState
        var startupStateBuilder = startupState.buildUpon()
        if (!startupUnitComponents.uuidValidator.isValid(startupState.uuid)) {
            val uuid = startupUnitComponents.multiProcessSafeUuidProvider.readUuid().id
            startupStateBuilder = startupStateBuilder.withUuid(uuid)
            DebugLogger.info(tag, "Extracted new uuid from storage = $uuid")
        }
        if (startupState.deviceId.isNullOrEmpty()) {
            val deviceIdCandidate = startupUnitComponents.deviceIdGenerator.generateDeviceId()
            DebugLogger.info(tag, "Apply new deviceIdCandidate: $deviceIdCandidate")
            startupStateBuilder = startupStateBuilder
                .withDeviceId(deviceIdCandidate)
                .withDeviceIdHash(StringUtils.EMPTY)
        }
        startupStateBuilder.withHostUrlsFromClient(
            startupUnitComponents.requestConfigArguments.newCustomHosts?.takeIf { it.isNotEmpty() }
        )
        val updatedStartupState = startupStateBuilder.build()
        DebugLogger.info(
            tag,
            "generate identifiers if needed - uuid: ${startupState.uuid} -> ${updatedStartupState.uuid}; " +
                "deviceId: ${startupState.deviceId} -> ${updatedStartupState.deviceId}.",
        )
        updateCurrentStartupDataAndNotifyListener(updatedStartupState)
    }

    override fun getContext(): Context {
        return startupUnitComponents.context
    }

    override fun getComponentId(): ComponentId {
        return startupUnitComponents.componentId
    }

    @Synchronized
    fun getOrCreateStartupTaskIfRequired(): NetworkTask? = if (isStartupRequired()) {
        DebugLogger.info(tag, "getOrCreateStartupTaskIfRequired - startupRequired")
        if (currentTask == null) {
            DebugLogger.info(tag, "getOrCreateStartupTaskIfRequired - create startup task")
            currentTask = createStartupTask(this, requestConfig)
        }
        currentTask
    } else {
        null
    }

    @Synchronized
    fun isStartupRequired(
        identifiers: List<String>?,
        clidsFromClientForVerification: Map<String, String>
    ): Boolean {
        return !StartupRequiredUtils.containsIdentifiers(
            startupState,
            identifiers,
            clidsFromClientForVerification
        ) { startupUnitComponents.clidsStorage }
    }

    @Synchronized
    fun isStartupRequired(): Boolean {
        val startupState = startupState
        var required = StartupRequiredUtils.isOutdated(startupState)
        DebugLogger.info(tag, "isStartupOutdated: $required")
        if (!required) {
            required = !StartupRequiredUtils.areMainIdentifiersValid(startupState)
            val validClids = startupUnitComponents.clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                requestConfig.clidsFromClient,
                startupState,
                startupUnitComponents.clidsStorage
            )
            DebugLogger.info(tag, " is startup required because of main identifiers being empty? $required")
            if (!required && !validClids) {
                DebugLogger.info(tag, "Startup is required because of clids")
                required = true
            }
        } else {
            DebugLogger.info(tag, "Startup required because it's outdated.")
        }
        return required
    }

    @Synchronized
    private fun removeCurrentStartupTask() {
        currentTask = null
    }

    override fun onRequestComplete(
        result: StartupResult,
        requestConfig: StartupRequestConfig,
        responseHeaders: Map<String, List<String>>?
    ) {
        var newState: StartupState
        synchronized(this) {
            val serverTime = StartupParser.parseServerTime(responseHeaders) ?: 0L
            updateServerTime(result.validTimeDifference, serverTime)
            newState = parseStartupResult(result, requestConfig, serverTime)
            DebugLogger.info(tag, " new state $newState")
            removeCurrentStartupTask()
            updateCurrentStartupData(newState)
        }
        notifyListener(newState)
    }

    @VisibleForTesting
    fun parseStartupResult(
        result: StartupResult,
        requestConfig: StartupRequestConfig,
        serverTime: Long
    ): StartupState {
        val clientClidsForRequest = StartupUtils.encodeClids(requestConfig.clidsFromClient)
        val chosenForRequestClids = requestConfig.chosenClids.clids
        val validClidsFromResponse = chooseValidClids(
            result.encodedClids,
            startupState.encodedClidsFromResponse
        )
        DebugLogger.info(tag, "Selected clids: $validClidsFromResponse")
        val deviceID = startupState.deviceId?.takeIf { it.isNotBlank() } ?: result.deviceId

        return StartupState.Builder(StartupStateBuilder(result.collectionFlags))
            .withDeviceId(deviceID)
            .withDeviceIdHash(result.deviceIDHash)
            .withObtainTime(startupUnitComponents.timeProvider.currentTimeSeconds())
            .withUuid(startupState.uuid)
            .withGetAdUrl(result.getAdUrl)
            .withHostUrlsFromStartup(result.startupUrls)
            .withHostUrlsFromClient(requestConfig.startupHostsFromClient)
            .withReportUrls(result.reportHostUrls)
            .withReportAdUrl(result.reportAdUrl)
            .withCertificateUrl(result.certificateUrl)
            .withDiagnosticUrls(result.diagnosticUrls)
            .withCustomSdkHosts(result.customSdkHosts)
            .withEncodedClidsFromResponse(validClidsFromResponse)
            .withLastClientClidsForStartupRequest(clientClidsForRequest)
            .withStartupDidNotOverrideClids(
                startupUnitComponents.clidsStateChecker.doRequestClidsMatchResponseClids(
                    chosenForRequestClids,
                    validClidsFromResponse
                )
            )
            .withLastChosenForRequestClids(StartupUtils.encodeClids(chosenForRequestClids))
            .withCountryInit(result.countryInit)
            .withPermissionsCollectingConfig(result.permissionsCollectingConfig)
            .withStatSending(result.statSending)
            .withHadFirstStartup(true)
            .withObtainServerTime(serverTime)
            .withFirstStartupServerTime(this.requestConfig.getOrSetFirstStartupTime(serverTime))
            .withOutdated(false)
            .withRetryPolicyConfig(result.retryPolicyConfig)
            .withCacheControl(result.cacheControl)
            .withAutoInappCollectingConfig(result.autoInappCollectingConfig)
            .withAttributionConfig(result.attributionConfig)
            .withStartupUpdateConfig(result.startupUpdateConfig)
            .withModulesRemoteConfigs(result.modulesRemoteConfigs)
            .withExternalAttributionConfig(result.externalAttributionConfig)
            .build()
    }

    private fun chooseValidClids(newClids: String?, oldClids: String?): String? =
        if (StartupUtils.isValidClids(newClids)) {
            newClids
        } else if (StartupUtils.isValidClids(oldClids)) {
            oldClids
        } else {
            null
        }

    private fun updateServerTime(
        validTimeDifference: Long?,
        serverTime: Long
    ) {
        ServerTime.getInstance().updateServerTime(serverTime, validTimeDifference)
    }

    private fun updateCurrentStartupDataAndNotifyListener(state: StartupState) {
        updateCurrentStartupData(state)
        notifyListener(state)
    }

    @Synchronized
    private fun updateCurrentStartupData(state: StartupState) {
        startupUnitComponents.startupConfigurationHolder.updateStartupState(state)
        startupUnitComponents.startupStateStorage.save(state)
        startupUnitComponents.startupStateHolder.onStartupStateChanged(state)
        DebugLogger.info(
            tag,
            "Startup was updated for package: %s; startupState: %s",
            startupUnitComponents.packageName,
            state
        )
    }

    private fun notifyListener(state: StartupState) {
        startupUnitComponents.resultListener.onStartupChanged(startupUnitComponents.packageName, state)
    }

    override fun onRequestError(cause: StartupError) {
        removeCurrentStartupTask()
        startupUnitComponents.resultListener.onStartupError(componentId.getPackage(), cause, startupState)
    }

    @Synchronized
    fun updateConfiguration(arguments: StartupRequestConfig.Arguments) {
        DebugLogger.info(
            tag,
            " update configuration for %s. New configuration %s",
            startupUnitComponents.componentId.toString(),
            arguments
        )
        startupUnitComponents.startupConfigurationHolder.updateArguments(arguments)
        findHosts(requestConfig)
    }

    private fun findHosts(config: StartupRequestConfig) {
        if (config.hasNewCustomHosts()) {
            val customHostUrls = config.newCustomHosts
            if (customHostUrls.isNullOrEmpty()) {
                if (config.startupHostsFromClient?.isNotEmpty() == true) {
                    DebugLogger.info(tag, "Reset custom hosts")
                    updateCurrentStartupDataAndNotifyListener(
                        startupState.buildUpon().withHostUrlsFromClient(null).build()
                    )
                }
            } else {
                if (!Utils.areEqual(customHostUrls, config.startupHostsFromClient)) {
                    DebugLogger.info(tag, "Update custom hosts to $customHostUrls")
                    updateCurrentStartupDataAndNotifyListener(
                        startupState.buildUpon().withHostUrlsFromClient(customHostUrls).build()
                    )
                }
            }
        }
    }
}
