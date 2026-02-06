package io.appmetrica.analytics.impl.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
internal class SimplePermissionExtractorTest(private val permissionName: String) : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context: Context by contextRule
    private val shouldAskSystemForPermissionStrategy: PermissionStrategy = mock()
    private val permissionExtractor by setUp { SimplePermissionExtractor(shouldAskSystemForPermissionStrategy) }

    @Test
    fun shouldAskSystemIsInvoked() {
        invokePermissionExtractorCurrentMethod()
        verify(shouldAskSystemForPermissionStrategy).forbidUsePermission(permissionName)
    }

    @Test
    fun doNotCallSystemWhenForbidden() {
        whenever(shouldAskSystemForPermissionStrategy.forbidUsePermission(permissionName))
            .thenReturn(true)
        val actual = invokePermissionExtractorCurrentMethod()
        verifyNoMoreInteractions(context)
        assertThat(actual).isFalse()
    }

    @Test
    fun whenAllowedToAskAndDisabledInSystem() {
        whenever(shouldAskSystemForPermissionStrategy.forbidUsePermission(permissionName))
            .thenReturn(true)
        whenever(context.checkCallingOrSelfPermission(permissionName))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        assertThat(invokePermissionExtractorCurrentMethod()).isFalse()
    }

    @Test
    fun whenAllowedToAskAndEnabledInSystem() {
        whenever(shouldAskSystemForPermissionStrategy.forbidUsePermission(permissionName))
            .thenReturn(true)
        whenever(context.checkCallingOrSelfPermission(permissionName))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        assertThat(invokePermissionExtractorCurrentMethod()).isFalse()
    }

    private fun invokePermissionExtractorCurrentMethod(): Boolean {
        return permissionExtractor.hasPermission(context, permissionName)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            arrayOf(Manifest.permission.ACCESS_WIFI_STATE)
        )
    }
}
