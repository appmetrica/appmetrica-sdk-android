package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class RemoteConfigPermissionStrategyTest : CommonTest() {

    private lateinit var remoteConfigPermissionStrategy: RemoteConfigPermissionStrategy

    @Test
    fun `shouldAsk for initial empty set`() {
        remoteConfigPermissionStrategy = RemoteConfigPermissionStrategy()
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission("some")).isTrue()
    }

    @Test
    fun `shouldAsk for updated empty`() {
        val permission = "first"
        remoteConfigPermissionStrategy = RemoteConfigPermissionStrategy()
        remoteConfigPermissionStrategy.updatePermissions(emptySet())
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission(permission)).isTrue()
    }

    @Test
    fun `shouldAsk for updated without target`() {
        val first = "first"
        remoteConfigPermissionStrategy = RemoteConfigPermissionStrategy()
        remoteConfigPermissionStrategy.updatePermissions(setOf("second"))
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission(first)).isTrue()
    }

    @Test
    fun `shouldAsk for updated with target`() {
        val first = "first"
        remoteConfigPermissionStrategy = RemoteConfigPermissionStrategy()
        remoteConfigPermissionStrategy.updatePermissions(setOf(first))
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission(first)).isFalse()
    }
}
