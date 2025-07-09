package io.appmetrica.analytics.impl.component.processor.commutation

import android.os.Bundle
import android.os.ResultReceiver
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.IdentifiersData
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ForceStartupHandlerTest : CommonTest() {

    private val advIdTrackingEnabled = true
    private val dataSendingEnabled = true

    private val counterConfiguration: CounterConfiguration = mock {
        on { isAdvIdentifiersTrackingEnabled } doReturn advIdTrackingEnabled
        on { dataSendingEnabled } doReturn dataSendingEnabled
    }

    private val reporterArguments = CommonArguments.ReporterArguments(counterConfiguration, emptyMap())

    private val commutationDispatcherComponent: CommutationDispatcherComponent = mock {
        on { configuration } doReturn reporterArguments
    }

    private val commutationClientUnit: CommutationClientUnit = mock {
        on { component } doReturn commutationDispatcherComponent
    }

    private val identifiersData: IdentifiersData = IdentifiersData(
        emptyList(),
        emptyMap(),
        mock<ResultReceiver>(),
        true
    )

    private val bundle = Bundle().apply {
        putParcelable(IdentifiersData.BUNDLE_KEY, identifiersData)
    }

    private val counterReport: CounterReport = mock {
        on { payload } doReturn bundle
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val forceStartupHandler by setUp { ForceStartupHandler(commutationDispatcherComponent) }

    @Test
    fun nullBundle() {
        whenever(counterReport.payload).thenReturn(null)
        forceStartupHandler.process(counterReport, commutationClientUnit)
        verify(commutationDispatcherComponent).provokeStartupOrGetCurrentState(null)
    }

    @Test
    fun emptyBundle() {
        whenever(counterReport.payload).thenReturn(Bundle())
        forceStartupHandler.process(counterReport, commutationClientUnit)
        verify(commutationDispatcherComponent).provokeStartupOrGetCurrentState(null)
    }

    @Test
    fun filledBundle() {
        val identifiersData = Mockito.mock(IdentifiersData::class.java)
        val bundle = Bundle()
        bundle.putParcelable(IdentifiersData.BUNDLE_KEY, identifiersData)
        whenever(counterReport.payload).thenReturn(bundle)
        forceStartupHandler.process(counterReport, commutationClientUnit)
        verify(commutationDispatcherComponent).provokeStartupOrGetCurrentState(identifiersData)
    }

    @Test
    fun `process if force send refresh is true`() {
        forceStartupHandler.process(counterReport, commutationClientUnit)
        verify(GlobalServiceLocator.getInstance().advertisingIdGetter).setInitialStateFromClientConfigIfNotDefined(true)
        verify(GlobalServiceLocator.getInstance().dataSendingRestrictionController)
            .setEnabledFromMainReporterIfNotYet(true)
    }

    @Test
    fun `process if force send refresh is false`() {
        val identifierDataWithFalse = IdentifiersData(
            emptyList(),
            emptyMap(),
            mock(),
            false
        )
        bundle.putParcelable(IdentifiersData.BUNDLE_KEY, identifierDataWithFalse)
        forceStartupHandler.process(counterReport, commutationClientUnit)
        val arguments = CommonArguments.ReporterArguments(counterConfiguration, emptyMap())
        whenever(commutationDispatcherComponent.configuration).thenReturn(arguments)
        verifyNoInteractions(
            GlobalServiceLocator.getInstance().advertisingIdGetter,
            GlobalServiceLocator.getInstance().dataSendingRestrictionController
        )
    }

    @Test
    fun `process force send refresh if payload is null`() {
        whenever(counterReport.payload).thenReturn(null)
        forceStartupHandler.process(counterReport, commutationClientUnit)
        verifyNoInteractions(
            GlobalServiceLocator.getInstance().advertisingIdGetter,
            GlobalServiceLocator.getInstance().dataSendingRestrictionController
        )
    }
}
