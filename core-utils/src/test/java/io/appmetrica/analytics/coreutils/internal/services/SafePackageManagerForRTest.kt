package io.appmetrica.analytics.coreutils.internal.services

import android.content.pm.InstallSourceInfo
import android.content.pm.PackageManager
import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stubbing
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.R])
class SafePackageManagerForRTest : CommonTest() {

    private val packageManager: PackageManager = mock()
    private val installSourceInfo: InstallSourceInfo = mock()

    private val packageName = "com.test.package.name"
    private val installingPackageName = "com.test.installing.package.name"

    @Before
    fun setUp() {
        stubbing(packageManager) {
            on { getInstallSourceInfo(packageName) } doReturn installSourceInfo
        }
    }

    @Test
    fun extractPackageInstaller() {
        stubbing(installSourceInfo) {
            on { installingPackageName } doReturn installingPackageName
        }
        assertThat(SafePackageManagerHelperForR.extractPackageInstaller(packageManager, packageName))
            .isEqualTo(installingPackageName)
    }

    @Test
    fun extractPackageInstallerForNull() {
        stubbing(installSourceInfo) {
            on { initiatingPackageName } doReturn null
        }
        assertThat(SafePackageManagerHelperForR.extractPackageInstaller(packageManager, packageName))
            .isEqualTo(null)
    }

    @Test
    fun extractPackageInstallerForEmpty() {
        stubbing(installSourceInfo) {
            on { installingPackageName } doReturn ""
        }
        assertThat(SafePackageManagerHelperForR.extractPackageInstaller(packageManager, packageName))
            .isEqualTo("")
    }

    @Test
    fun extractPackageInstallerIfPackageManagerThrowsException() {
        stubbing(packageManager) {
            on { getInstallSourceInfo(packageName) } doThrow RuntimeException("Some exception")
        }
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { SafePackageManagerHelperForR.extractPackageInstaller(packageManager, packageName) }
    }

    @Test
    fun extractPackageInstallerIfInstallSourceThrowsException() {
        stubbing(installSourceInfo) {
            on { installingPackageName } doThrow RuntimeException("Some exception")
        }
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { SafePackageManagerHelperForR.extractPackageInstaller(packageManager, packageName) }
    }
}
