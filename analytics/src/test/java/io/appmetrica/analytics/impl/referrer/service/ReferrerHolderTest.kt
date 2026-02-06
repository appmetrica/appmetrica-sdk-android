package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.nullable
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ReferrerHolderTest : CommonTest() {
    private val listener: ReferrerListenerNotifier = mock()
    private val anotherListener: ReferrerListenerNotifier = mock()

    @get:Rule
    val referrerAggregatorConstructionRule = constructionRule<ReferrerAggregator>()
    private val referrerAggregator: ReferrerAggregator by referrerAggregatorConstructionRule

    @get:Rule
    val contextRule = ContextRule()
    private val context: Context by contextRule
    private val referrerFromServices = ReferrerInfo("referrer from services", 100, 200, ReferrerInfo.Source.GP)
    private val newReferrerFromServices =
        ReferrerInfo("new referrer from play services", 110, 220, ReferrerInfo.Source.HMS)

    private val vitalCommonDataProvider: VitalCommonDataProvider = mock {
        on { referrer }.thenReturn(referrerFromServices)
    }

    @Test
    fun retrieveReferrerChecked() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(true)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        holder.retrieveReferrerIfNeeded()
        assertThat(referrerAggregatorConstructionRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun retrieveReferrerNotChecked() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(false)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        holder.retrieveReferrerIfNeeded()
        verify(referrerAggregator).retrieveReferrer()
        assertThat(referrerAggregatorConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, holder)
    }

    @Test
    fun initialReferrerIsNull() {
        whenever(vitalCommonDataProvider.referrer).thenReturn(null)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        assertThat(holder.referrerInfo).isNull()
    }

    @Test
    fun initialStateIsHasFromPlayServices() {
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        assertThat(holder.referrerInfo).isEqualTo(referrerFromServices)
    }

    @Test
    fun storeReferrer() {
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        holder.subscribe(listener)
        holder.storeReferrer(newReferrerFromServices)
        verify(vitalCommonDataProvider).referrerChecked = true
        verify(vitalCommonDataProvider).referrer = newReferrerFromServices
        verify(listener).notifyIfNeeded(newReferrerFromServices)
        assertThat(holder.referrerInfo).isEqualTo(newReferrerFromServices)
    }

    @Test
    fun storeNullReferrer() {
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        holder.subscribe(listener)
        clearInvocations(listener)
        holder.storeReferrer(null)
        verify(vitalCommonDataProvider).referrer = null
        verify(vitalCommonDataProvider).referrerChecked = true
        verify(listener).notifyIfNeeded(null)
        assertThat(holder.referrerInfo).isNull()
    }

    @Test
    fun subscribeSeveralListenersBeforeReceivingReferrer() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(false)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        holder.subscribe(listener)
        holder.subscribe(anotherListener)
        holder.storeReferrer(newReferrerFromServices)
        verify(listener).notifyIfNeeded(newReferrerFromServices)
        verify(anotherListener).notifyIfNeeded(newReferrerFromServices)
    }

    @Test
    fun subscribeSeveralListenersAfterReceivingReferrer() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(false)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        holder.storeReferrer(newReferrerFromServices)
        holder.subscribe(listener)
        holder.subscribe(anotherListener)
        verify(listener).notifyIfNeeded(newReferrerFromServices)
        verify(anotherListener).notifyIfNeeded(newReferrerFromServices)
    }

    @Test
    fun notifyListenerReferrerInfoIsNullReferrerChecked() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(true)
        whenever(vitalCommonDataProvider.referrer).thenReturn(null)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        assertThat(holder.referrerInfo).isNull()
        holder.subscribe(listener)
        verify(listener).notifyIfNeeded(null)
    }

    @Test
    fun notifyListenerReferrerInfoIsNullReferrerNotChecked() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(false)
        whenever(vitalCommonDataProvider.referrer).thenReturn(null)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        assertThat(holder.referrerInfo).isNull()
        holder.subscribe(listener)
        verify(listener, never()).notifyIfNeeded(nullable(ReferrerInfo::class.java))
    }

    @Test
    fun notifyListenerReferrerInfoIsNotNullReferrerChecked() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(true)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        assertThat(holder.referrerInfo).isEqualTo(referrerFromServices)
        holder.subscribe(listener)
        verify(listener).notifyIfNeeded(referrerFromServices)
    }

    @Test
    fun notifyListenerReferrerInfoIsNotNullReferrerNotChecked() {
        whenever(vitalCommonDataProvider.referrerChecked).thenReturn(false)
        val holder = ReferrerHolder(context, vitalCommonDataProvider)
        assertThat(holder.referrerInfo).isEqualTo(referrerFromServices)
        holder.subscribe(listener)
        verify(listener, never())
            .notifyIfNeeded(nullable(ReferrerInfo::class.java))
    }
}
