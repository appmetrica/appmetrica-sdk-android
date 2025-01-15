package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert
import java.util.UUID
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class MainReporterEventConditionTest : CommonTest() {

    private val configFromHolder: ReportRequestConfig = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock {
        on { get() } doReturn configFromHolder
    }
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    private val framework = "Framework"

    @get:Rule
    val frameworkDetectorMockedRule = staticRule<FrameworkDetector> {
        on { FrameworkDetector.framework() } doReturn framework
    }

    private val sdkReporter: IReporterExtended = mock()

    @get:Rule
    val appMetricaSelfReporterFacadeRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn sdkReporter
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val eventName = "activation_unlock_event_sending"
    private val offsetSinceCreation = 100500L

    private lateinit var condition: MainReporterEventCondition

    @Before
    fun setUp() {
        whenever(
            GlobalServiceLocator.getInstance().serviceLifecycleTimeTracker
                .offsetInSecondsSinceCreation(TimeUnit.SECONDS)
        ).thenReturn(offsetSinceCreation)
    }

    @Test
    fun `isConditionMet for initial state`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        assertThat(condition.isConditionMet).isFalse()
    }

    @Test
    fun `isConditionMet for initial state if true from prefs`() {
        whenever(preferences.getMainReporterEventsTriggerConditionMet(any())).thenReturn(true)
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        assertThat(condition.isConditionMet).isTrue()
    }

    @Test
    fun `isConditionMet for initial state if anonymous api key in initial config`() {
        condition = MainReporterEventCondition(
            configurationHolder,
            createArgumentsWithApiKey(DefaultValues.ANONYMOUS_API_KEY),
            preferences
        )
        assertThat(condition.isConditionMet).isFalse()
    }

    @Test
    fun `isConditionMet for initial state if non anonymous api key in initial config`() {
        condition = MainReporterEventCondition(
            configurationHolder,
            createArgumentsWithApiKey(UUID.randomUUID().toString()),
            preferences
        )
        assertThat(condition.isConditionMet).isTrue()
    }

    @Test
    fun `isConditionMet for anonymous api key in configuration holder`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(DefaultValues.ANONYMOUS_API_KEY)
        assertThat(condition.isConditionMet).isFalse()
    }

    @Test
    fun `isConditionMet for non anonymous api key in configuration holder`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        assertThat(condition.isConditionMet).isTrue()
        verify(preferences).putMainReporterEventsTriggerConditionMet(true)
        clearInvocations(preferences)
        assertThat(condition.isConditionMet).isTrue()
        verifyNoInteractions(preferences)
    }

    @Test
    fun `isConditionMet for non anonymous api key in configuration holder provoke sdk event`() {
        val stringCaptor = argumentCaptor<String>()
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        condition.isConditionMet
        condition.isConditionMet
        verify(sdkReporter).reportEvent(eq(eventName), stringCaptor.capture())

        JSONAssert.assertEquals(
            expectedJson("activation").toString(),
            stringCaptor.firstValue,
            true
        )
        assertThat(stringCaptor.allValues.size).isEqualTo(1)
    }

    @Test
    fun `isConditionMet for non anonymous api key in configuration holder after timer`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        condition.setConditionMetByTimer()
        clearInvocations(preferences, sdkReporter)
        condition.isConditionMet
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `isConditionMet for non anonymous api key in configuration holder after true from preferences`() {
        whenever(preferences.getMainReporterEventsTriggerConditionMet(any())).thenReturn(true)
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        clearInvocations(preferences)
        condition.isConditionMet
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `isConditionMet for api key in configuration holder after api key in initial config`() {
        condition = MainReporterEventCondition(
            configurationHolder,
            createArgumentsWithApiKey(UUID.randomUUID().toString()),
            preferences
        )
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        clearInvocations(preferences)
        condition.isConditionMet
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `isConditionMet for api key in configuration holder after anonymous api key in initial config`() {
        condition = MainReporterEventCondition(
            configurationHolder,
            createArgumentsWithApiKey(DefaultValues.ANONYMOUS_API_KEY),
            preferences
        )
        clearInvocations(preferences, sdkReporter)
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        condition.isConditionMet
        verify(preferences).putMainReporterEventsTriggerConditionMet(true)
        verify(sdkReporter).reportEvent(any(), any<String>())
    }

    @Test
    fun `setConditionMetByTimer for initial state`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        condition.setConditionMetByTimer()
        verify(preferences).putMainReporterEventsTriggerConditionMet(true)

        val stringCaptor = argumentCaptor<String>()
        verify(sdkReporter).reportEvent(eq(eventName), stringCaptor.capture())

        JSONAssert.assertEquals(
            expectedJson("timer").toString(),
            stringCaptor.firstValue,
            true
        )
        assertThat(stringCaptor.allValues.size).isEqualTo(1)
        clearInvocations(preferences, sdkReporter)

        assertThat(condition.isConditionMet).isTrue()
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `setConditionMetByTimer after true from preferences`() {
        whenever(preferences.getMainReporterEventsTriggerConditionMet(any())).thenReturn(true)
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        clearInvocations(preferences, sdkReporter)
        condition.setConditionMetByTimer()
        condition.isConditionMet
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `setConditionMetByTimer for initial config with anonymous api key`() {
        condition = MainReporterEventCondition(
            configurationHolder,
            createArgumentsWithApiKey(DefaultValues.ANONYMOUS_API_KEY),
            preferences
        )
        condition.isConditionMet
        clearInvocations(preferences)
        condition.setConditionMetByTimer()
        assertThat(condition.isConditionMet).isTrue()
        verify(preferences).putMainReporterEventsTriggerConditionMet(true)
        verify(sdkReporter).reportEvent(any(), any<String>())
    }

    @Test
    fun `setConditionMetByTimer for initial config with api key`() {
        condition = MainReporterEventCondition(
            configurationHolder,
            createArgumentsWithApiKey(UUID.randomUUID().toString()),
            preferences
        )
        clearInvocations(preferences)
        condition.setConditionMetByTimer()
        assertThat(condition.isConditionMet).isTrue()
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `setConditionMetByTimer for api key in config holder`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(UUID.randomUUID().toString())
        condition.isConditionMet
        clearInvocations(preferences, sdkReporter)
        condition.setConditionMetByTimer()
        assertThat(condition.isConditionMet).isTrue()
        verifyNoInteractions(preferences, sdkReporter)
    }

    @Test
    fun `setConditionMetByTimer for anonymous api key in config holder`() {
        condition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)
        whenever(configFromHolder.apiKey).thenReturn(DefaultValues.ANONYMOUS_API_KEY)
        condition.isConditionMet
        clearInvocations(preferences, sdkReporter)
        condition.setConditionMetByTimer()
        assertThat(condition.isConditionMet).isTrue()
        verify(preferences).putMainReporterEventsTriggerConditionMet(true)
        verify(sdkReporter).reportEvent(any(), any<String>())
    }

    private fun expectedJson(source: String): JSONObject = JSONObject().apply {
        put("source", source)
        put("framework", framework)
        put("activation_offset", offsetSinceCreation)
    }

    private fun createArgumentsWithApiKey(apiKey: String?): CommonArguments.ReporterArguments {
        val counterConfiguration: CounterConfiguration = mock {
            on { getApiKey() } doReturn apiKey
        }
        return CommonArguments.ReporterArguments(counterConfiguration, null)
    }
}
