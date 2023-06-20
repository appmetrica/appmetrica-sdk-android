package io.appmetrica.analytics.coreutils.internal.permission

import android.Manifest
import android.content.Context
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
class AnyOfPermissionStrategyTest(
    private val inputPermissions: Array<String>,
    private val grantedPermissions: List<String>,
    private val expectedValue: Boolean
) {

    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun data(): List<Array<Any>> = listOf(
            arrayOf(emptyArray<String>(), emptyList<String>(), true),
            arrayOf(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), emptyList<String>(), false),
            arrayOf(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                listOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                true
            ),
            arrayOf(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE),
                listOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                true
            )
        )
    }

    private val context = mock<Context>()
    private val permissionExtractor = mock<PermissionExtractor>()

    private lateinit var strategy: AnyOfPermissionStrategy

    @Before
    fun setUp() {
        grantedPermissions.forEach {
            whenever(permissionExtractor.hasPermission(context, it)).thenReturn(true)
        }
        strategy = AnyOfPermissionStrategy(permissionExtractor, *inputPermissions)
    }

    @Test
    fun hasNecessaryPermissions() {
        assertThat(strategy.hasNecessaryPermissions(context)).isEqualTo(expectedValue)
    }
}
