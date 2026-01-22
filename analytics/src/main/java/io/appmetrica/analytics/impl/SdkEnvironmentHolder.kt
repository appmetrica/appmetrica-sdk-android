package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreapi.internal.model.AppVersionInfo
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo
import io.appmetrica.analytics.coreapi.internal.model.SdkEnvironment
import io.appmetrica.analytics.coreapi.internal.model.SdkInfo
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.impl.utils.DeviceTypeProvider
import java.util.concurrent.CopyOnWriteArrayList

internal class SdkEnvironmentHolder(private val context: Context) : SdkEnvironmentProvider {

    internal interface Listener {
        fun onSdkEnvironmentChanged()
    }

    private val localeExtractor: LocaleExtractor = LocaleExtractor()

    private val listeners = CopyOnWriteArrayList<Listener>()

    override lateinit var sdkEnvironment: SdkEnvironment
        private set

    init {
        sdkEnvironment = SdkEnvironment(
            appVersionInfo = AppVersionInfo(
                PackageManagerUtils.getAppVersionName(context),
                PackageManagerUtils.getAppVersionCodeString(context)
            ),
            appFramework = FrameworkDetector.framework(),
            screenInfo = ScreenInfo(0, 0, 0, 0f),
            sdkInfo = SdkInfo(
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_NUMBER,
                SdkUtils.formSdkBuildType()
            ),
            deviceType = DeviceTypeValues.PHONE,
            locales = localeExtractor.extractLocales(context.resources.configuration)
        )
    }

    private var deviceTypeFromClient: String? = null

    @Synchronized
    fun mayBeUpdateScreenInfo(screenInfo: ScreenInfo?) {
        if (screenInfo != null && screenInfo != sdkEnvironment.screenInfo) {
            val deviceType = deviceTypeFromClient ?: DeviceTypeProvider.getDeviceType(
                context,
                Point(screenInfo.width, screenInfo.height)
            )
            sdkEnvironment = sdkEnvironment.copy(screenInfo = screenInfo, deviceType = deviceType)
            notifyListeners()
        }
    }

    @Synchronized
    fun mayBeUpdateDeviceTypeFromClient(deviceType: String?) {
        deviceType?.let {
            if (it != deviceTypeFromClient) {
                deviceTypeFromClient = it
                if (it != sdkEnvironment.deviceType) {
                    sdkEnvironment = sdkEnvironment.copy(deviceType = it)
                    notifyListeners()
                }
            }
        }
    }

    @Synchronized
    fun mayBeUpdateAppVersion(appVersionName: String?, appBuildNumber: String?) {
        val appVersionNameCandidate = appVersionName ?: sdkEnvironment.appVersionInfo.appVersionName
        val appBuildNumberCandidate = appBuildNumber ?: sdkEnvironment.appVersionInfo.appBuildNumber

        if (!sdkEnvironment.appVersionInfo.equalsTo(appVersionNameCandidate, appBuildNumberCandidate)) {
            sdkEnvironment =
                sdkEnvironment.copy(appVersionInfo = AppVersionInfo(appVersionNameCandidate, appBuildNumberCandidate))
            notifyListeners()
        }
    }

    @Synchronized
    fun mayBeUpdateConfiguration(configuration: Configuration) {
        val locales = localeExtractor.extractLocales(configuration)

        if (sdkEnvironment.locales != locales) {
            sdkEnvironment = sdkEnvironment.copy(locales = locales)
            notifyListeners()
        }
    }

    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onSdkEnvironmentChanged() }
    }

    private fun AppVersionInfo.equalsTo(appVersionName: String, appBuildNumber: String): Boolean {
        return this.appVersionName == appVersionName && this.appBuildNumber == appBuildNumber
    }
}
