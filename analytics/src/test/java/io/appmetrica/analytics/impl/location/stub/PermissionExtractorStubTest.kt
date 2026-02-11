package io.appmetrica.analytics.impl.location.stub

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions

internal class PermissionExtractorStubTest : CommonTest() {

    private val context = mock<Context>()
    private val permission = "Some permission"

    private lateinit var permissionExtractorStub: PermissionExtractorStub

    @Before
    fun setUp() {
        permissionExtractorStub = PermissionExtractorStub()
    }

    @Test
    fun hasPermission() {
        assertThat(permissionExtractorStub.hasPermission(context, permission)).isFalse()
        verifyNoMoreInteractions(context)
    }
}
