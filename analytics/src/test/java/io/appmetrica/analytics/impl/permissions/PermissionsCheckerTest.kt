package io.appmetrica.analytics.impl.permissions

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn

internal class PermissionsCheckerTest : CommonTest() {

    private val context: Context = mock()
    private val firstPermissionState = listOf(mock<PermissionState>())
    private val secondPermissionState = listOf(mock<PermissionState>())

    @get:Rule
    val runtimePermissionRetrieverMockedConstructionRule = constructionRule<RuntimePermissionsRetriever> {
        on { permissionsState } doReturn firstPermissionState
    }

    private val permissionsChecker by setUp { PermissionsChecker() }

    @Test
    fun `check if the same`() {
        assertThat(permissionsChecker.check(context, firstPermissionState)).isNull()
    }

    @Test
    fun `check if changed`() {
        assertThat(permissionsChecker.check(context, secondPermissionState)).isSameAs(firstPermissionState)
    }
}
