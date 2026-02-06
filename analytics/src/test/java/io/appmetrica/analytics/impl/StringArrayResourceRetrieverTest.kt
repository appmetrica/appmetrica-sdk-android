package io.appmetrica.analytics.impl

import android.content.res.Resources
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.stubbing

internal class StringArrayResourceRetrieverTest : CommonTest() {

    private val resourceName = "my_resource"

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val resources: Resources by lazy { context.resources }

    private val resourceRetriever by setUp { StringArrayResourceRetriever(context, resourceName) }

    @Test
    fun hasResource() {
        stubbing(resources) {
            on { getIdentifier(eq(resourceName), eq("array"), any()) } doReturn 0
        }
        assertThat(resourceRetriever.resource).isNull()
    }

    @Test
    fun resourceOK() {
        val resId = 10
        val array = arrayOf("aaa", "bbb")
        stubbing(resources) {
            on { getIdentifier(eq(resourceName), eq("array"), any()) } doReturn resId
            on { getStringArray(resId) } doReturn array
        }
        assertThat(resourceRetriever.resource).isEqualTo(array)
    }

    @Test
    fun getStringArrayThrows() {
        val resId = 10
        stubbing(resources) {
            on { getIdentifier(eq(resourceName), eq("array"), any()) } doReturn resId
            on { getStringArray(resId) } doThrow RuntimeException()
        }
        assertThat(resourceRetriever.resource).isNull()
    }
}
