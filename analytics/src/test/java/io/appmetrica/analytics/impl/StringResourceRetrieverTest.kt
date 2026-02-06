package io.appmetrica.analytics.impl

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.util.UUID

internal class StringResourceRetrieverTest : CommonTest() {
    private val resourceName = "resourceName"

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val stringResourceRetriever by setUp { StringResourceRetriever(context, resourceName) }

    @Test
    fun hasResource() {
        whenever(context.resources.getIdentifier(eq(resourceName), eq("string"), anyString()))
            .thenReturn(0)
        assertThat(stringResourceRetriever.getResource()).isNull()
    }

    @Test
    fun resourceOK() {
        val resId = 10
        val buildId = UUID.randomUUID().toString()
        whenever(context.resources.getIdentifier(eq(resourceName), eq("string"), anyString()))
            .thenReturn(resId)
        whenever(context.getString(resId)).thenReturn(buildId)
        assertThat(stringResourceRetriever.getResource()).isEqualTo(buildId)
    }
}
