package io.appmetrica.analytics.coreutils.internal.permission

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SinglePermissionStrategyTest : CommonTest() {

    private val permission = "Some permission"

    private val context = mock<Context>()
    private val permissionExtractor = mock<PermissionExtractor>()

    private lateinit var singlePermissionStrategy: SinglePermissionStrategy

    @Before
    fun setUp() {
        singlePermissionStrategy = SinglePermissionStrategy(permissionExtractor, permission)
    }

    @Test
    fun `hasNecessaryPermissions if allow`() {
        whenever(permissionExtractor.hasPermission(context, permission)).thenReturn(true)
        assertThat(singlePermissionStrategy.hasNecessaryPermissions(context)).isTrue()
    }

    @Test
    fun `hasNecessaryPermissions if forbidden`() {
        whenever(permissionExtractor.hasPermission(context, permission)).thenReturn(false)
        assertThat(singlePermissionStrategy.hasNecessaryPermissions(context)).isFalse()
    }
}
