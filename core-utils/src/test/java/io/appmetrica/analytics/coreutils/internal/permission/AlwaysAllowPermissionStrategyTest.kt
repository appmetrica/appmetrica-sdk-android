package io.appmetrica.analytics.coreutils.internal.permission

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions

class AlwaysAllowPermissionStrategyTest : CommonTest() {

    private val context = mock<Context>()

    private lateinit var strategy: AlwaysAllowPermissionStrategy

    @Before
    fun setUp() {
        strategy = AlwaysAllowPermissionStrategy()
    }

    @Test
    fun hasNecessaryPermissions() {
        assertThat(strategy.hasNecessaryPermissions(context)).isTrue()
        verifyNoMoreInteractions(context)
    }
}
