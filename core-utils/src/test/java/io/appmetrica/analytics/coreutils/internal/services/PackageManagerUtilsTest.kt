package io.appmetrica.analytics.coreutils.internal.services

import android.content.Context
import android.content.pm.PackageInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PackageManagerUtilsTest : CommonTest() {

    private val testPackageName = "test.package.name"
    private val wrongTestPackageName = "wrong.test.package.name"
    private val testVersionCode = 100500
    private val testVersionName = "100.500"

    private val context = mock<Context> {
        on { packageName } doReturn testPackageName
    }

    private val wrongContext = mock<Context> {
        on { packageName } doReturn wrongTestPackageName
    }

    private val packageInfo = PackageInfo().apply {
        this.packageName = testPackageName
        this.versionName = testVersionName
        this.versionCode = testVersionCode
    }

    @get:Rule
    val packageManagerMockedRule = MockedConstructionRule(SafePackageManager::class.java) { mock, mockedContext ->
        whenever(mock.getPackageInfo(any(), eq(testPackageName))).thenReturn(packageInfo)
        whenever(mock.getPackageInfo(any(), eq(wrongTestPackageName))).thenReturn(null)
    }

    @Test
    fun getPackageInfo() {
        assertThat(PackageManagerUtils.getPackageInfo(context)).isNotNull
    }

    @Test
    fun `getPackageInfo if null`() {
        assertThat(PackageManagerUtils.getPackageInfo(wrongContext)).isNull()
    }

    @Test
    fun getAppVersionCode() {
        assertThat(PackageManagerUtils.getAppVersionCodeInt(context)).isEqualTo(testVersionCode)
    }

    @Test
    fun `getAppVersionCode for null packageInfo`() {
        assertThat(PackageManagerUtils.getAppVersionCodeInt(wrongContext)).isEqualTo(0)
    }

    @Test
    fun getAppVersionCodeString() {
        assertThat(PackageManagerUtils.getAppVersionCodeString(context)).isEqualTo(testVersionCode.toString())
    }

    @Test
    fun `getAppVersionCodeString for null packageInfo`() {
        assertThat(PackageManagerUtils.getAppVersionCodeString(wrongContext)).isEqualTo("0")
    }

    @Test
    fun getAppVersionName() {
        assertThat(PackageManagerUtils.getAppVersionName(context)).isEqualTo(testVersionName)
    }

    @Test
    fun `getAppVersionName for null packageInfo`() {
        assertThat(PackageManagerUtils.getAppVersionName(wrongContext)).isEqualTo("0.0")
    }
}
