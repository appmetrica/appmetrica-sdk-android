package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ModuleSelfReporterImplTest : CommonTest() {

    @get:Rule
    val selfReporterFacadeMockedRule = MockedStaticRule(AppMetricaSelfReportFacade::class.java)

    private val reporter = mock<IReporterExtended>()
    private val moduleEventArgumentCaptor = argumentCaptor<ModuleEvent>()

    private val eventType = 100500
    private val eventName = "event name"
    private val throwable = mock<Throwable>()
    private val mapValue = mapOf("key" to "value")
    private val stringValue = "string value"
    private val stringErrorMessage = "string error message"
    private val errorIdentifier = "error identifier"

    private lateinit var moduleSelfReporterImpl: ModuleSelfReporterImpl

    @Before
    fun setUp() {
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(reporter)

        moduleSelfReporterImpl = ModuleSelfReporterImpl()
    }

    @Test
    fun reportEvent() {
        moduleSelfReporterImpl.reportEvent(eventName)
        verify(reporter).reportEvent(eventName)
    }

    @Test
    fun reportEventWithMapValue() {
        moduleSelfReporterImpl.reportEvent(eventName, mapValue)
        verify(reporter).reportEvent(eventName, mapValue)
    }

    @Test
    fun reportEventWithType() {
        moduleSelfReporterImpl.reportEvent(eventType, eventName, stringValue)
        verify(reporter).reportEvent(moduleEventArgumentCaptor.capture())

        assertThat(moduleEventArgumentCaptor.allValues).hasSize(1)

        ObjectPropertyAssertions(moduleEventArgumentCaptor.firstValue)
            .checkField("type", eventType)
            .checkField("name", eventName)
            .checkField("value", stringValue)
            .checkField("metricaServiceDataReporterType", MetricaServiceDataReporter.TYPE_CORE)
            .checkFieldsAreNull("environment", "extras", "attributes")
            .checkAll()
    }

    @Test
    fun reportEventWithStringValue() {
        moduleSelfReporterImpl.reportEvent(eventName, stringValue)
        verify(reporter).reportEvent(eventName, stringValue)
    }

    @Test
    fun reportErrorWithThrowable() {
        moduleSelfReporterImpl.reportError(stringErrorMessage, throwable)
        verify(reporter).reportError(stringErrorMessage, throwable)
    }

    @Test
    fun reportErrorWithNullThrowable() {
        moduleSelfReporterImpl.reportError(stringErrorMessage, null as Throwable?)
        verify(reporter).reportError(stringErrorMessage, null as Throwable?)
    }

    @Test
    fun reportErrorWithIdentifier() {
        moduleSelfReporterImpl.reportError(errorIdentifier, stringErrorMessage)
        verify(reporter).reportError(errorIdentifier, stringErrorMessage)
    }

    @Test
    fun reportErrorWithIdentifierAndNullMessage() {
        moduleSelfReporterImpl.reportError(errorIdentifier, null as String?)
        verify(reporter).reportError(errorIdentifier, null as String?)
    }
}
