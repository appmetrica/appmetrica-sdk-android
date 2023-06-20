package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Resources
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StringArrayResourceRetrieverTest : CommonTest() {

    private val resourceName = "my_resource"

    private val resources = mock<Resources>()
    private lateinit var context: Context

    private lateinit var resourceRetriever: StringArrayResourceRetriever

    @Before
    fun setUp() {
        context = TestUtils.createMockedContext()
        stubbing(context) {
            on { resources } doReturn resources
        }
        resourceRetriever = StringArrayResourceRetriever(context, resourceName)
    }

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
