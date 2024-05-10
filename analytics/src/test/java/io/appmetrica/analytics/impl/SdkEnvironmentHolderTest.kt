package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreapi.internal.model.AppVersionInfo
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo
import io.appmetrica.analytics.coreapi.internal.model.SdkInfo
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SdkEnvironmentHolderTest : CommonTest() {

    private val configuration = mock<Configuration>()

    private val resources = mock<Resources> {
        on { configuration } doReturn configuration
    }

    private val context: Context = mock {
        on { resources } doReturn resources
        on { packageName } doReturn "package_name"
    }

    private val locales = listOf("First", "Second")

    private val defaultAppVersionInt = 343534
    private val defaultAppBuildNumber = defaultAppVersionInt.toString()
    private val defaultAppVersion = "default app version"

    @get:Rule
    val packageManagerUtilsMockedStaticRule = staticRule<PackageManagerUtils> {
        on { PackageManagerUtils.getAppVersionName(context) } doReturn defaultAppVersion
        on { PackageManagerUtils.getAppVersionCodeString(context) } doReturn defaultAppBuildNumber
    }

    @get:Rule
    val localeExtractorMockedConstructionRule = constructionRule<LocaleExtractor> {
        on { extractLocales(configuration) } doReturn locales
    }

    private val localeExtractor: LocaleExtractor by localeExtractorMockedConstructionRule

    private val appFramework = "app framework"

    @get:Rule
    val frameworkDetectorMockedStaticRule = staticRule<FrameworkDetector> {
        on { FrameworkDetector.framework() } doReturn appFramework
    }

    @get:Rule
    val phoneUtilsMockedStaticRule = staticRule<PhoneUtils> {
        on { PhoneUtils.getDeviceType(any(), any()) } doReturn DeviceTypeValues.PHONE
    }

    private val sdkBuildType = "sdk_build_type"

    @get:Rule
    val sdkBuildTypeMockedStaticRule = staticRule<SdkUtils> {
        on { SdkUtils.formSdkBuildType() } doReturn sdkBuildType
    }

    private val listener: SdkEnvironmentHolder.Listener = mock()

    private val sdkEnvironmentHolder: SdkEnvironmentHolder by setUp { SdkEnvironmentHolder(context) }

    @Before
    fun setUp() {
        sdkEnvironmentHolder.registerListener(listener)
    }

    @Test
    fun `initial sdkEnvironment`() {
        checkSdkEnvironmentIsDefault()
    }

    @Test
    fun mayBeUpdateScreenInfo() {
        val newScreenInfo = ScreenInfo(100, 200, 300, 400f)
        sdkEnvironmentHolder.mayBeUpdateScreenInfo(newScreenInfo)

        ObjectPropertyAssertions(sdkEnvironmentHolder.sdkEnvironment.screenInfo)
            .checkField("width", newScreenInfo.width)
            .checkField("height", newScreenInfo.height)
            .checkField("dpi", newScreenInfo.dpi)
            .checkField("scaleFactor", newScreenInfo.scaleFactor)
            .checkAll()

        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun `mayBeUpdateScreenInfo if null`() {
        sdkEnvironmentHolder.mayBeUpdateScreenInfo(null)
        verifyNoInteractions(listener)
    }

    @Test
    fun `mayBeUpdateScreenInfo if not changed`() {
        val old = sdkEnvironmentHolder.sdkEnvironment.screenInfo
        sdkEnvironmentHolder.mayBeUpdateScreenInfo(
            ScreenInfo(old.width, old.height, old.dpi, old.scaleFactor)
        )
        checkSdkEnvironmentIsDefault()
        verifyNoInteractions(listener)
    }

    @Test
    fun `mayBeUpdateScreenInfo if deviceType changed`() {
        val point = Point(100, 200)
        val newScreenInfo = ScreenInfo(point.x, point.y, 300, 400f)
        whenever(PhoneUtils.getDeviceType(context, point)).thenReturn(DeviceTypeValues.TV)
        sdkEnvironmentHolder.mayBeUpdateScreenInfo(newScreenInfo)
        assertThat(sdkEnvironmentHolder.sdkEnvironment.deviceType).isEqualTo(DeviceTypeValues.TV)
        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun mayBeUpdateDeviceTypeFromClient() {
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(DeviceTypeValues.TV)
        assertThat(sdkEnvironmentHolder.sdkEnvironment.deviceType).isEqualTo(DeviceTypeValues.TV)
        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun `mayBeUpdateDeviceTypeFromClient if null`() {
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(null)
        assertThat(sdkEnvironmentHolder.sdkEnvironment.deviceType).isEqualTo(DeviceTypeValues.PHONE)
        verifyNoInteractions(listener)
    }

    @Test
    fun `mayBeUpdateDeviceTypeFromClient if not changed`() {
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(DeviceTypeValues.TV)
        clearInvocations(listener)
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(DeviceTypeValues.TV)
        assertThat(sdkEnvironmentHolder.sdkEnvironment.deviceType).isEqualTo(DeviceTypeValues.TV)
        verifyNoInteractions(listener)
    }

    @Test
    fun `mayBeUpdateDeviceTypeFromClient if the same as initial value`() {
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(DeviceTypeValues.PHONE)
        assertThat(sdkEnvironmentHolder.sdkEnvironment.deviceType).isEqualTo(DeviceTypeValues.PHONE)
        verifyNoInteractions(listener)
    }

    @Test
    fun `maybeBeUpdateScreenInfo after mayBeUpdateDeviceTypeFromClient`() {
        sdkEnvironmentHolder.mayBeUpdateDeviceTypeFromClient(DeviceTypeValues.TV)
        val point = Point(100, 200)
        val newScreenInfo = ScreenInfo(point.x, point.y, 300, 400f)
        whenever(PhoneUtils.getDeviceType(context, point)).thenReturn(DeviceTypeValues.CAR)
        sdkEnvironmentHolder.mayBeUpdateScreenInfo(newScreenInfo)

        assertThat(sdkEnvironmentHolder.sdkEnvironment.deviceType).isEqualTo(DeviceTypeValues.TV)
    }

    @Test
    fun mayBeUpdateAppVersion() {
        val newVersionName = "new version name"
        val newBuildNumber = "new build number"

        sdkEnvironmentHolder.mayBeUpdateAppVersion(newVersionName, newBuildNumber)

        val appVersionInfo = sdkEnvironmentHolder.sdkEnvironment.appVersionInfo
        assertThat(appVersionInfo.appVersionName).isEqualTo(newVersionName)
        assertThat(appVersionInfo.appBuildNumber).isEqualTo(newBuildNumber)

        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun `mayBeUpdateAppVersion if both are null`() {
        sdkEnvironmentHolder.mayBeUpdateAppVersion(null, null)

        val appVersionInfo = sdkEnvironmentHolder.sdkEnvironment.appVersionInfo
        assertThat(appVersionInfo.appVersionName).isEqualTo(defaultAppVersion)
        assertThat(appVersionInfo.appBuildNumber).isEqualTo(defaultAppBuildNumber)

        verifyNoInteractions(listener)
    }

    @Test
    fun `mayBeUpdateAppVersion if version name is null`() {
        val newBuildNumber = "new build number"
        sdkEnvironmentHolder.mayBeUpdateAppVersion(null, newBuildNumber)

        val appVersionInfo = sdkEnvironmentHolder.sdkEnvironment.appVersionInfo
        assertThat(appVersionInfo.appVersionName).isEqualTo(defaultAppVersion)
        assertThat(appVersionInfo.appBuildNumber).isEqualTo(newBuildNumber)

        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun `mayBeUpdateAppVersion if build number is null`() {
        val newVersionName = "new version name"
        sdkEnvironmentHolder.mayBeUpdateAppVersion(newVersionName, null)

        val appVersionInfo = sdkEnvironmentHolder.sdkEnvironment.appVersionInfo
        assertThat(appVersionInfo.appVersionName).isEqualTo(newVersionName)
        assertThat(appVersionInfo.appBuildNumber).isEqualTo(defaultAppBuildNumber)

        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun `mayBeUpdateAppVersion if not changed`() {
        sdkEnvironmentHolder.mayBeUpdateAppVersion(defaultAppVersion, defaultAppBuildNumber)

        val appVersionInfo = sdkEnvironmentHolder.sdkEnvironment.appVersionInfo
        assertThat(appVersionInfo.appVersionName).isEqualTo(defaultAppVersion)
        assertThat(appVersionInfo.appBuildNumber).isEqualTo(defaultAppBuildNumber)

        verifyNoInteractions(listener)
    }

    @Test
    fun mayBeUpdateConfiguration() {
        val newLocales = listOf("new first locale", "new second locale")
        val newConfiguration: Configuration = mock()
        whenever(localeExtractor.extractLocales(newConfiguration)).thenReturn(newLocales)

        sdkEnvironmentHolder.mayBeUpdateConfiguration(newConfiguration)

        assertThat(sdkEnvironmentHolder.sdkEnvironment.locales).isEqualTo(newLocales)
        verify(listener).onSdkEnvironmentChanged()
    }

    @Test
    fun `mayBeUpdateConfiguration if not changed`() {
        val newConfiguration: Configuration = mock()
        whenever(localeExtractor.extractLocales(newConfiguration)).thenReturn(locales)

        sdkEnvironmentHolder.mayBeUpdateConfiguration(newConfiguration)
        assertThat(sdkEnvironmentHolder.sdkEnvironment.locales).isEqualTo(locales)

        verifyNoInteractions(listener)
    }

    @Test
    fun listeners() {
        val secondListener: SdkEnvironmentHolder.Listener = mock()
        sdkEnvironmentHolder.registerListener(secondListener)
        sdkEnvironmentHolder.unregisterListener(listener)
        sdkEnvironmentHolder.mayBeUpdateAppVersion("100500", "100500")

        verify(secondListener).onSdkEnvironmentChanged()
        verifyNoInteractions(listener)
    }

    private fun checkSdkEnvironmentIsDefault() {
        ObjectPropertyAssertions(sdkEnvironmentHolder.sdkEnvironment)
            .checkFieldRecursively<AppVersionInfo>("appVersionInfo") {
                it
                    .checkField("appVersionName", defaultAppVersion)
                    .checkField("appBuildNumber", defaultAppBuildNumber)
            }
            .checkField("appFramework", appFramework)
            .checkFieldRecursively<ScreenInfo>("screenInfo") {
                it
                    .checkField("width", 0)
                    .checkField("height", 0)
                    .checkField("dpi", 0)
                    .checkField("scaleFactor", 0f)
            }
            .checkFieldRecursively<SdkInfo>("sdkInfo") {
                it
                    .checkField("sdkVersionName", BuildConfig.VERSION_NAME)
                    .checkField("sdkBuildNumber", BuildConfig.BUILD_NUMBER)
                    .checkField(
                        "sdkBuildType",
                        sdkBuildType
                    )
            }
            .checkField("deviceType", DeviceTypeValues.PHONE)
            .checkField("locales", locales)
            .checkAll()
    }
}
