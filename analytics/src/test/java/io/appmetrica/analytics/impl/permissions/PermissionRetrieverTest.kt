package io.appmetrica.analytics.impl.permissions

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

internal class PermissionRetrieverTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule
    private var packageInfo: PackageInfo = PackageInfo()
    private val packageManager: PackageManager by lazy { context.packageManager }

    @Before
    fun setUp() {
        whenever(packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS))
            .thenReturn(packageInfo)
    }

    @Test
    fun permissionsCheckerWithEqualPermissions() {
        val checker = PermissionsChecker()
        packageInfo.requestedPermissions = arrayOf("permissionA", "permissionB")
        packageInfo.requestedPermissionsFlags = intArrayOf(PackageInfo.REQUESTED_PERMISSION_GRANTED, 0)
        val result = checker.check(
            context,
            listOf(
                PermissionState("permissionA", true),
                PermissionState("permissionB", false)
            )
        )
        assertThat(result).isNull()
    }

    @Test
    fun permissionsCheckerWithChangedState() {
        val checker = PermissionsChecker()
        packageInfo.requestedPermissions = arrayOf("permissionA", "permissionB")
        packageInfo.requestedPermissionsFlags = intArrayOf(PackageInfo.REQUESTED_PERMISSION_GRANTED, 0)
        val result = checker.check(
            context,
            listOf(
                PermissionState("permissionA", true),
                PermissionState("permissionB", true)
            )
        )
        assertThat(result).extracting("name", "granted")
            .contains(
                tuple("permissionA", true),
                tuple("permissionB", false)
            )
    }

    @Test
    fun permissionsCheckerWithChangedPermissionsList() {
        val checker = PermissionsChecker()
        packageInfo.requestedPermissions = arrayOf(
            "permissionA",
            "permissionB",
            "permissionC"
        )
        packageInfo.requestedPermissionsFlags = intArrayOf(
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0
        )
        val result = checker.check(
            context,
            listOf(
                PermissionState("permissionA", true),
                PermissionState("permissionB", true)
            )
        )
        assertThat(result).extracting("name", "granted")
            .contains(
                tuple("permissionA", true),
                tuple("permissionB", true),
                tuple("permissionC", false)
            )
    }

    @Test
    fun runtimePermissionRetriever() {
        packageInfo.requestedPermissions = arrayOf(
            "permissionA",
            "permissionB",
            "permissionC",
            "permissionD"
        )
        packageInfo.requestedPermissionsFlags = intArrayOf(
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0,
            PackageInfo.REQUESTED_PERMISSION_GRANTED,
            0
        )
        val retriever = RuntimePermissionsRetriever(context)
        val permissions = retriever.getPermissionsState()
        assertThat(permissions).size().isEqualTo(4)
        assertThat(permissions).extracting("granted").contains(true, false, true, false)
    }
}
