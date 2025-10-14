package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrier
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrierCallback
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
internal class PluginMainReporterEventSendingPolicyTest : CommonTest() {

    private val eventTrigger: EventTrigger = mock()
    private val triggerProvider: EventTriggerProvider = mock {
        on { eventTrigger } doReturn eventTrigger
    }

    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val activationBarrier: ActivationBarrier by setUp {
        GlobalServiceLocator.getInstance().activationBarrier
    }

    private val executor: IHandlerExecutor = mock()

    private val activationCallbackCaptor = argumentCaptor<ActivationBarrierCallback>()

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor)
            .thenReturn(executor)
    }

    @Test
    fun `condition is not met`() {
        val mainReporterConditionConstructionMock = Mockito.mockConstruction(
            MainReporterEventCondition::class.java
        ) { mock, _ -> whenever(mock.isConditionMet).thenReturn(false) }
        val policy = PluginMainReporterEventSendingPolicy(
            triggerProvider,
            configurationHolder,
            initialConfig,
            preferences
        )

        verify(activationBarrier).subscribe(
            eq(TimeUnit.SECONDS.toMillis(DefaultValues.ANONYMOUS_API_KEY_EVENT_SENDING_DELAY_SECONDS)),
            eq(executor),
            activationCallbackCaptor.capture()
        )
        verify(mainReporterConditionConstructionMock.constructed().first(), never()).setConditionMetByTimer()
        verify(eventTrigger, never()).trigger()

        assertThat(activationCallbackCaptor.allValues.size).isEqualTo(1)
        activationCallbackCaptor.firstValue.onWaitFinished()
        verify(mainReporterConditionConstructionMock.constructed().first()).setConditionMetByTimer()
        verify(eventTrigger).trigger()

        assertThat(policy.condition).isEqualTo(mainReporterConditionConstructionMock.constructed().first())

        mainReporterConditionConstructionMock.close()
    }

    @Test
    fun `condition is met`() {
        val mainReporterConditionConstructionMock = Mockito.mockConstruction(
            MainReporterEventCondition::class.java
        ) { mock, _ -> whenever(mock.isConditionMet).thenReturn(true) }

        val policy = PluginMainReporterEventSendingPolicy(
            triggerProvider,
            configurationHolder,
            initialConfig,
            preferences
        )

        verifyNoInteractions(activationBarrier)
        verify(mainReporterConditionConstructionMock.constructed().first(), never()).setConditionMetByTimer()
        verify(eventTrigger, never()).trigger()

        assertThat(policy.condition).isEqualTo(mainReporterConditionConstructionMock.constructed().first())

        mainReporterConditionConstructionMock.close()
    }
}
