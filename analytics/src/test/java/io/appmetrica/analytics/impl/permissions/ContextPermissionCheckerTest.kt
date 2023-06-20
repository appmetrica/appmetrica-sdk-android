package io.appmetrica.analytics.impl.permissions

import android.content.Context
import android.content.pm.PackageManager
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

private const val PERMISSION = "Test permission"

class ContextPermissionCheckerTest : CommonTest() {
    val context = mock<Context>()

    @Test
    fun hasPermissionForGranted() {
        stubbing(context) {
            on { checkCallingOrSelfPermission(PERMISSION) } doReturn PackageManager.PERMISSION_GRANTED
        }
        assertThat(ContextPermissionChecker.hasPermission(context, PERMISSION)).isTrue
    }

    @Test
    fun hasPermissionForDenied() {
        stubbing(context) {
            on { checkCallingOrSelfPermission(PERMISSION) } doReturn PackageManager.PERMISSION_DENIED
        }
        assertThat(ContextPermissionChecker.hasPermission(context, PERMISSION)).isFalse
    }

    @Test
    fun hasPermissionForException() {
        stubbing(context) {
            on { checkCallingOrSelfPermission(PERMISSION) } doThrow RuntimeException()
        }
        assertThat(ContextPermissionChecker.hasPermission(context, PERMISSION)).isFalse
    }
}
