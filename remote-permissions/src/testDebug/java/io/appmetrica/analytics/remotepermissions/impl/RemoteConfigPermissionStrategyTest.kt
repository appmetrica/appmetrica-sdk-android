package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class RemoteConfigPermissionStrategyTest : CommonTest() {

    private val remoteConfigPermissionStrategy = RemoteConfigPermissionStrategy()

    @Test
    fun `shouldAsk for initial empty set`() {
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission("some")).isTrue()
    }

    @Test
    fun `shouldAsk for updated empty`() {
        val permission = "first"
        remoteConfigPermissionStrategy.updatePermissions(emptySet())
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission(permission)).isTrue()
    }

    @Test
    fun `shouldAsk for updated without target`() {
        val first = "first"
        remoteConfigPermissionStrategy.updatePermissions(setOf("second"))
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission(first)).isTrue()
    }

    @Test
    fun `shouldAsk for updated with target`() {
        val first = "first"
        remoteConfigPermissionStrategy.updatePermissions(setOf(first))
        assertThat(remoteConfigPermissionStrategy.forbidUsePermission(first)).isFalse()
    }
}
