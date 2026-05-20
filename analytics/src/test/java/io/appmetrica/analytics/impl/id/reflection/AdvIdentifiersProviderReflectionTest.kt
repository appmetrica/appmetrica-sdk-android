package io.appmetrica.analytics.impl.id.reflection

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@SuppressLint("RobolectricUsage") // stubbing AdvIdentifiersProvider
@RunWith(RobolectricTestRunner::class)
internal class AdvIdentifiersProviderReflectionTest : CommonTest() {

    private val advIdentifiersProviderClass = "io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider"

    private val reflectionAdvIdParser = mock<ReflectionAdvIdParser>()

    @get:Rule
    val advIdentifiersProviderStaticRule = MockedStaticRule(AdvIdentifiersProvider::class.java)

    @get:Rule
    val reflectionUtilsStaticRule = staticRule<ReflectionUtils> {
        on { ReflectionUtils.detectClassExists(advIdentifiersProviderClass) } doReturn true
    }

    private val context = mock<Context>()
    private val providerReflection = AdvIdentifiersProviderReflection(reflectionAdvIdParser)

    @Test
    fun `isAvailable returns true when class exists`() {
        assertThat(providerReflection.isAvailable()).isTrue()
    }

    @Test
    fun `isAvailable returns false when class does not exist`() {
        whenever(ReflectionUtils.detectClassExists(advIdentifiersProviderClass)).thenReturn(false)
        assertThat(providerReflection.isAvailable()).isFalse()
    }

    @Test
    fun `tryToGetAdTrackingInfoBundle returns bundle from provider`() {
        val providerName = "google"
        val resultBundle = mock<Bundle>()
        val resultAdTrackingInfo = mock<AdTrackingInfoResult>()
        whenever(reflectionAdvIdParser.fromBundle(resultBundle)).thenReturn(resultAdTrackingInfo)

        stubbing(advIdentifiersProviderStaticRule.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) } doReturn resultBundle
        }

        assertThat(providerReflection.requestIdentifiers(context, providerName))
            .isSameAs(resultAdTrackingInfo)
    }

    @Test
    fun `tryToGetAdTrackingInfoBundle returns null when provider returns null`() {
        val providerName = "google"
        whenever(reflectionAdvIdParser.fromBundle(null)).thenReturn(null)

        stubbing(advIdentifiersProviderStaticRule.staticMock) {
            on { AdvIdentifiersProvider.requestIdentifiers(same(context), any()) } doReturn null
        }

        assertThat(providerReflection.requestIdentifiers(context, providerName)).isNull()
    }
}
