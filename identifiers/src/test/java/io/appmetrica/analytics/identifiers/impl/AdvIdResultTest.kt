package io.appmetrica.analytics.identifiers.impl

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdvIdResultTest {

    @get:Rule
    val rule: MockitoRule = MockitoJUnit.rule()

    @Mock
    internal lateinit var info: AdvIdInfo

    @Mock
    internal lateinit var trackingBundle: Bundle

    @Before
    fun setUp() {
        doReturn(trackingBundle).whenever(info).toBundle()
    }

    @Test
    fun fullInfo() {
        val status = IdentifierStatus.OK
        val error = "asodjasdo"

        val data = AdvIdResult(status, info, error).toBundle()

        val soft = SoftAssertions()

        soft.assertThat(data.getString(Constants.STATUS)).`as`("status").isEqualTo(status.value)
        soft.assertThat(data.getBundle(Constants.TRACKING_INFO)).`as`("tracking").isSameAs(trackingBundle)
        soft.assertThat(data.getString(Constants.ERROR_MESSAGE)).`as`("error message").isEqualTo(error)
        soft.assertThat(data.size()).`as`("size").isEqualTo(3)

        soft.assertAll()
    }

    @Test
    fun emptyInfo() {
        val status = IdentifierStatus.OK

        val data = AdvIdResult(status).toBundle()

        val soft = SoftAssertions()

        soft.assertThat(data.getString(Constants.STATUS)).`as`("status").isEqualTo(status.value)
        soft.assertThat(data.getBundle(Constants.TRACKING_INFO)).`as`("tracking").isNull()
        soft.assertThat(data.getString(Constants.ERROR_MESSAGE)).`as`("error message").isNull()
        soft.assertThat(data.size()).`as`("size").isEqualTo(2)

        soft.assertAll()
    }
}
