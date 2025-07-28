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

    @get:Rule
    val sSdkUtils = MockedStaticRule(ReflectionUtils::class.java)

    @Test
    fun getBillingTypeIfLibraryAndVersion3() {
        BuildConfig.VERSION_NAME = "3.0.3"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }

    @Test
    fun getBillingTypeIfLibraryAndVersion4() {
        BuildConfig.VERSION_NAME = "4.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }

    @Test
    fun getBillingTypeIfLibraryAndNoVersion() {
        BuildConfig.VERSION_NAME = ""
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }

    @Test
    fun getBillingTypeIfLibraryAndVersion5() {
        BuildConfig.VERSION_NAME = "5.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V6)
    }

    @Test
    fun getBillingTypeIfLibraryAndVersion6() {
        BuildConfig.VERSION_NAME = "6.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V6)
    }

    @Test
    fun getBillingTypeIfLibraryAndVersion7() {
        BuildConfig.VERSION_NAME = "7.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V6)
    }

    @Test
    fun getBillingTypeIfLibraryAndVersion8() {
        BuildConfig.VERSION_NAME = "8.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V8)
    }

    @Test
    fun getBillingTypeIfLibraryAndWrongVersion() {
        BuildConfig.VERSION_NAME = "999.0.0"
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(BuildConfig::class.java)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.LIBRARY_V8)
    }

    @Test
    fun getBillingTypeIfNotLibrary() {
        whenever(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(null)
        assertThat(BillingTypeDetector.getBillingType()).isEqualTo(BillingType.NONE)
    }
}
