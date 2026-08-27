package io.appmetrica.analytics.impl.referrer.service.provider.google

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class GoogleReferrerProviderTest : CommonTest() {
    private val libClass = "com.android.installreferrer.api.InstallReferrerClient"
    private val context: Context = mock()
    private val executor: ICommonExecutor = mock()

    @get:Rule
    val reflectionUtilsRule = staticRule<ReflectionUtils>()

    @get:Rule
    val googlePlayReferrerLibraryRule = constructionRule<GooglePlayReferrerLibrary>()
    private val googlePlayReferrerLibrary by googlePlayReferrerLibraryRule

    private val googleReferrerProvider by setUp { GoogleReferrerProvider(context, executor) }

    @Test
    fun `referrerName returns google`() {
        assertThat(googleReferrerProvider.referrerName).isEqualTo("google")
    }

    @Test
    fun `requestReferrer calls GooglePlayReferrerLibrary when library is available`() {
        val listener: ReferrerListener = mock()

        whenever(ReflectionUtils.detectClassExists(libClass)) doReturn true

        googleReferrerProvider.requestReferrer(listener)

        verify(googlePlayReferrerLibrary).requestReferrer(context, listener)
    }

    @Test
    fun `requestReferrer returns Failure when library is not available`() {
        whenever(ReflectionUtils.detectClassExists(libClass)) doReturn false

        val listener: ReferrerListener = mock()
        googleReferrerProvider.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Google Play Install Referrer library is not detected")
    }
}
