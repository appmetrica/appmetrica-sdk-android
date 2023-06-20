package io.appmetrica.analytics.impl.permissions

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CompositePermissionStrategyTest : CommonTest() {

    private val permission = "Permission"

    private val firstInternalStrategy = mock<PermissionStrategy>()
    private val secondInternalStrategy = mock<PermissionStrategy>()

    private lateinit var strategy: CompositePermissionStrategy

    @Test
    fun `shouldAsk without strategies`() {
        strategy = CompositePermissionStrategy()
        assertThat(strategy.forbidUsePermission(permission)).isFalse()
    }

    @Test
    fun `shouldAsk for two forbid strategies`() {
        whenever(firstInternalStrategy.forbidUsePermission(permission)).thenReturn(false)
        whenever(secondInternalStrategy.forbidUsePermission(permission)).thenReturn(false)

        strategy = CompositePermissionStrategy(firstInternalStrategy, secondInternalStrategy)

        assertThat(strategy.forbidUsePermission(permission)).isFalse()
    }

    @Test
    fun `shouldAsk for single forbid strategy`() {
        whenever(firstInternalStrategy.forbidUsePermission(permission)).thenReturn(true)
        whenever(secondInternalStrategy.forbidUsePermission(permission)).thenReturn(false)

        strategy = CompositePermissionStrategy(firstInternalStrategy, secondInternalStrategy)

        assertThat(strategy.forbidUsePermission(permission)).isTrue()
    }

    @Test
    fun `shouldAsk for two allow strategies`() {
        whenever(firstInternalStrategy.forbidUsePermission(permission)).thenReturn(true)
        whenever(secondInternalStrategy.forbidUsePermission(permission)).thenReturn(true)

        strategy = CompositePermissionStrategy(firstInternalStrategy, secondInternalStrategy)

        assertThat(strategy.forbidUsePermission(permission)).isTrue()
    }
}
