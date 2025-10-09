package io.appmetrica.analytics.impl.startup

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig

internal class StartupState private constructor(
    val deviceId: String?,
    val deviceIdHash: String?,
    private val startupStateModel: StartupStateModel
) {

    val uuid: String? = startupStateModel.uuid
    val reportUrls: List<String>? = startupStateModel.reportUrls
    val hostUrlsFromStartup: List<String>? = startupStateModel.hostUrlsFromStartup
    val hostUrlsFromClient: List<String>? = startupStateModel.hostUrlsFromClient
    val diagnosticUrls: List<String>? = startupStateModel.diagnosticUrls
    val customSdkHosts: Map<String, List<String>>? = startupStateModel.customSdkHosts
    val getAdUrl: String? = startupStateModel.getAdUrl
    val reportAdUrl: String? = startupStateModel.reportAdUrl
    val certificateUrl: String? = startupStateModel.certificateUrl
    val encodedClidsFromResponse: String? = startupStateModel.encodedClidsFromResponse
    val lastClientClidsForStartupRequest: String? = startupStateModel.lastClientClidsForStartupRequest
    val lastChosenForRequestClids: String? = startupStateModel.lastChosenForRequestClids
    val collectingFlags: CollectingFlags = startupStateModel.collectingFlags
    val obtainTime: Long = startupStateModel.obtainTime
    val hadFirstStartup: Boolean = startupStateModel.hadFirstStartup
    val startupDidNotOverrideClids: Boolean = startupStateModel.startupDidNotOverrideClids
    val countryInit: String? = startupStateModel.countryInit
    val statSending: StatSending? = startupStateModel.statSending
    val permissionsCollectingConfig: PermissionsCollectingConfig? = startupStateModel.permissionsCollectingConfig
    val retryPolicyConfig: RetryPolicyConfig = startupStateModel.retryPolicyConfig
    val obtainServerTime: Long = startupStateModel.obtainServerTime
    val firstStartupServerTime: Long = startupStateModel.firstStartupServerTime
    val outdated: Boolean = startupStateModel.outdated
    val cacheControl: CacheControl? = startupStateModel.cacheControl
    val attributionConfig: AttributionConfig? = startupStateModel.attributionConfig
    val startupUpdateConfig: StartupUpdateConfig = startupStateModel.startupUpdateConfig
    val modulesRemoteConfigs: Map<String, Any> = startupStateModel.modulesRemoteConfigs
    val externalAttributionConfig: ExternalAttributionConfig? = startupStateModel.externalAttributionConfig

    fun buildUpon(): Builder = buildUpon(startupStateModel.collectingFlags)

    fun buildUpon(collectingFlags: CollectingFlags): Builder = Builder(startupStateModel.buildUpon(collectingFlags))
        .withDeviceId(deviceId)
        .withDeviceIdHash(deviceIdHash)

    override fun toString(): String {
        return "StartupState(deviceId=$deviceId, deviceIdHash=$deviceIdHash, startupStateModel=$startupStateModel)"
    }

    class Builder(private val modelBuilder: StartupStateModel.StartupStateBuilder) {

        private var deviceId: String? = null
        private var deviceIdHash: String? = null

        constructor(collectingFlags: CollectingFlags) : this(StartupStateModel.StartupStateBuilder(collectingFlags))

        fun withDeviceId(value: String?) = this.also { deviceId = value }
        fun withDeviceIdHash(value: String?) = this.also { deviceIdHash = value }
        fun withUuid(value: String?) = this.also { modelBuilder.withUuid(value) }
        fun withHostUrlsFromClient(value: List<String>?) = this.also { modelBuilder.withHostUrlsFromClient(value) }
        fun withReportUrls(value: List<String>?) = this.also { modelBuilder.withReportUrls(value) }
        fun withDiagnosticUrls(value: List<String>?) = this.also { modelBuilder.withDiagnosticUrls(value) }
        fun withHostUrlsFromStartup(value: List<String>?) = this.also { modelBuilder.withHostUrlsFromStartup(value) }
        fun withCustomSdkHosts(value: Map<String, List<String>>?) = this.also { modelBuilder.withCustomSdkHosts(value) }
        fun withGetAdUrl(value: String?) = this.also { modelBuilder.withGetAdUrl(value) }
        fun withReportAdUrl(value: String?) = this.also { modelBuilder.withReportAdUrl(value) }
        fun withCertificateUrl(value: String?) = this.also { modelBuilder.withCertificateUrl(value) }
        fun withEncodedClidsFromResponse(value: String?) = this.also {
            modelBuilder.withEncodedClidsFromResponse(value)
        }

        fun withLastClientClidsForStartupRequest(value: String?) = this.also {
            modelBuilder.withLastClientClidsForStartupRequest(value)
        }

        fun withLastChosenForRequestClids(value: String?) = this.also {
            modelBuilder.withLastChosenForRequestClids(value)
        }

        fun withObtainTime(value: Long) = this.also { modelBuilder.withObtainTime(value) }
        fun withObtainServerTime(value: Long) = this.also { modelBuilder.withObtainServerTime(value) }
        fun withFirstStartupServerTime(value: Long) = this.also { modelBuilder.withFirstStartupServerTime(value) }
        fun withHadFirstStartup(value: Boolean) = this.also { modelBuilder.withHadFirstStartup(value) }
        fun withOutdated(value: Boolean) = this.also { modelBuilder.withOutdated(value) }
        fun withStartupDidNotOverrideClids(value: Boolean) = this.also {
            modelBuilder.withStartupDidNotOverrideClids(value)
        }

        fun withCountryInit(value: String?) = this.also { modelBuilder.withCountryInit(value) }
        fun withStatSending(value: StatSending?) = this.also { modelBuilder.withStatSending(value) }
        fun withPermissionsCollectingConfig(value: PermissionsCollectingConfig?) = this.also {
            modelBuilder.withPermissionsCollectingConfig(value)
        }

        fun withRetryPolicyConfig(value: RetryPolicyConfig?) = this.also { modelBuilder.withRetryPolicyConfig(value) }

        fun withCacheControl(value: CacheControl?) = this.also { modelBuilder.withCacheControl(value) }

        fun withAttributionConfig(value: AttributionConfig?) = this.also { modelBuilder.withAttributionConfig(value) }

        fun withStartupUpdateConfig(value: StartupUpdateConfig?) =
            this.also { modelBuilder.withStartupUpdateConfig(value) }

        fun withModulesRemoteConfigs(value: Map<String, Any>) =
            this.also { modelBuilder.withModulesRemoteConfigs(value) }

        fun withExternalAttributionConfig(value: ExternalAttributionConfig?) =
            this.also { modelBuilder.withExternalAttributionConfig(value) }

        fun build(): StartupState = StartupState(deviceId, deviceIdHash, modelBuilder.build())
    }

    class Storage @VisibleForTesting constructor(
        private val modelStorage: ProtobufStateStorage<StartupStateModel>,
        private val vitalCommonDataProvider: VitalCommonDataProvider
    ) {

        constructor(context: Context) : this(
            StorageFactory.Provider.get(StartupStateModel::class.java).create(context),
            GlobalServiceLocator.getInstance().vitalDataProviderStorage.commonDataProvider
        )

        fun read(): StartupState {
            return StartupState(
                vitalCommonDataProvider.deviceId,
                vitalCommonDataProvider.deviceIdHash,
                modelStorage.read()
            )
        }

        fun save(startupState: StartupState) {
            vitalCommonDataProvider.deviceId = startupState.deviceId
            vitalCommonDataProvider.deviceIdHash = startupState.deviceIdHash
            modelStorage.save(startupState.startupStateModel)
        }
    }
}
