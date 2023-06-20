package io.appmetrica.analytics.impl.permissions

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RuntimePermissionsRetrieverTest : CommonTest() {

    private lateinit var context: Context
    @Mock
    private lateinit var safePackageManager: SafePackageManager
    private lateinit var retriever: RuntimePermissionsRetriever
    private val packageName = "test.package"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        `when`(context.packageName).thenReturn(packageName)
        retriever = RuntimePermissionsRetriever(context, safePackageManager)
    }

    @Test
    fun correctArgumentsAreUsed() {
        retriever.permissionsState
        verify(safePackageManager).getPackageInfo(context, packageName, PackageManager.GET_PERMISSIONS)
    }

    @Test
    fun getPermissionsStateNullPackageInfo() {
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(null)
        assertThat(retriever.permissionsState).isNotNull.isEmpty()
    }

    @Test
    fun getPermissionsStateNullPermissions() {
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = null
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).isNotNull.isEmpty()
    }

    @Test
    fun getPermissionsStateEmptyPermissions() {
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = emptyArray()
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).isNotNull.isEmpty()
    }

    @Test
    fun getPermissionsStateHasPermissionsNullFlags() {
        val permission1 = "permission.1"
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = arrayOf(permission1)
        packageInfo.requestedPermissionsFlags = null
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).containsExactly(PermissionState(permission1, false))
    }

    @Test
    fun getPermissionsStateHasPermissionsHasDeniedFlag() {
        val permission1 = "permission.1"
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = arrayOf(permission1)
        packageInfo.requestedPermissionsFlags = IntArray(1) { 0 }
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).containsExactly(PermissionState(permission1, false))
    }

    @Test
    fun getPermissionsStateHasPermissionsHasGrantedFlag() {
        val permission1 = "permission.1"
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = arrayOf(permission1)
        packageInfo.requestedPermissionsFlags = IntArray(1) { PackageInfo.REQUESTED_PERMISSION_GRANTED }
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).containsExactly(PermissionState(permission1, true))
    }

    @Test
    fun getPermissionsStateHasSeveralPermissions() {
        val permission1 = "permission.1"
        val permission2 = "permission.2"
        val permission3 = "permission.3"
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = arrayOf(permission1, permission2, permission3)
        packageInfo.requestedPermissionsFlags = intArrayOf(PackageInfo.REQUESTED_PERMISSION_GRANTED, 0, PackageInfo.REQUESTED_PERMISSION_GRANTED)
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).containsExactly(
            PermissionState(permission1, true),
            PermissionState(permission2, false),
            PermissionState(permission3, true),
        )
    }

    @Test
    fun getPermissionsStateHasLessFlagsThanPermissions() {
        val permission1 = "permission.1"
        val permission2 = "permission.2"
        val permission3 = "permission.3"
        val packageInfo = PackageInfo()
        packageInfo.requestedPermissions = arrayOf(permission1, permission2, permission3)
        packageInfo.requestedPermissionsFlags = intArrayOf(PackageInfo.REQUESTED_PERMISSION_GRANTED)
        `when`(safePackageManager.getPackageInfo(any<Context>(), any<String>(), any<Int>())).thenReturn(packageInfo)
        assertThat(retriever.permissionsState).containsExactly(
            PermissionState(permission1, true),
            PermissionState(permission2, false),
            PermissionState(permission3, false),
        )
    }
}
