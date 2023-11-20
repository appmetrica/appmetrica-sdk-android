package io.appmetrica.analytics.impl.billing

import com.android.billingclient.BuildConfig
import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

private const val BUILD_CONFIG_CLASS = "com.android.billingclient.BuildConfig"

class BillingTypeDetectorTest : CommonTest() {

    @Rule
    @JvmField
    val sSdkUtils = MockedStaticRule(ReflectionUtils::class.java)

    @Test
    fun testGetBillingTypeIfLibraryAndVersion3() {
        BuildConfig.VERSION_NAME = "3.0.3"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }

    @Test
    fun testGetBillingTypeIfLibraryAndVersion4() {
        BuildConfig.VERSION_NAME = "4.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }

    @Test
    fun testGetBillingTypeIfLibraryAndNoVersion() {
        BuildConfig.VERSION_NAME = ""
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }

    @Test
    fun testGetBillingTypeIfLibraryAndVersion5() {
        BuildConfig.VERSION_NAME = "5.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V6)
    }

    @Test
    fun testGetBillingTypeIfLibraryAndVersion6() {
        BuildConfig.VERSION_NAME = "6.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V6)
    }

    @Test
    fun testGetBillingTypeIfLibraryAndWrongVersion() {
        BuildConfig.VERSION_NAME = "999.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V6)
    }

    @Test
    fun testGetBillingTypeIfNotLibrary() {
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(null)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }
}
