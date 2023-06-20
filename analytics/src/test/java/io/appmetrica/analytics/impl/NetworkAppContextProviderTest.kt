package io.appmetrica.analytics.impl

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.networktasks.internal.NetworkAppContext
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkAppContextProviderTest {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val frameworkDetectorMockedRule = MockedStaticRule(FrameworkDetector::class.java)

    @get:Rule
    val localeHolderMockedRule = MockedStaticRule(LocaleHolder::class.java)

    val localeHolder = mock<LocaleHolder>()

    private lateinit var networkAppContextProvider: NetworkAppContextProvider
    private lateinit var networkAppContext: NetworkAppContext

    private val framework = "test framework"

    @Before
    fun setUp() {
        whenever(FrameworkDetector.framework()).thenReturn(framework)
        whenever(LocaleHolder.getInstance(GlobalServiceLocator.getInstance().context)).thenReturn(localeHolder)
        networkAppContextProvider = NetworkAppContextProvider()
        networkAppContext = networkAppContextProvider.getNetworkAppContext()
    }

    @Test
    fun sdkVersionName() {
        assertThat(networkAppContext.sdkInfo.sdkVersionName)
            .isEqualTo(BuildConfig.VERSION_NAME)
    }

    @Test
    fun sdkBuildNumber() {
        assertThat(networkAppContext.sdkInfo.sdkBuildNumber)
            .isEqualTo(BuildConfig.BUILD_NUMBER)
    }

    @Test
    fun sdkBuildType() {
        assertThat(networkAppContext.sdkInfo.sdkBuildType)
            .isEqualTo("${BuildConfig.SDK_BUILD_FLAVOR}_${BuildConfig.SDK_DEPENDENCY}_${BuildConfig.SDK_BUILD_TYPE}")
    }

    @Test
    fun appFramework() {
        assertThat(networkAppContext.appInfo.appFramework).isEqualTo(framework)
    }

    @Test
    fun screenInfoProvider() {
        assertThat(networkAppContext.screenInfoProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().screenInfoHolder)
    }

    @Test
    fun advertisingIdGetter() {
        assertThat(networkAppContext.advertisingIdGetter)
            .isEqualTo(GlobalServiceLocator.getInstance().serviceInternalAdvertisingIdGetter)
    }

    @Test
    fun localeProvider() {
        assertThat(networkAppContext.localeProvider).isEqualTo(localeHolder)
    }

    @Test
    fun appSetIdProvider() {
        assertThat(networkAppContext.appSetIdProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().appSetIdGetter)
    }
}
