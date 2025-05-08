package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ReportKotlinVersionTaskTest : CommonTest() {

    private val currentTimeMillis = 2321432L
    private val longAgoSendTime = 100500L
    private val nearestSendTime = 200500L
    private val kotlinVersionMajor = KotlinVersion.CURRENT.major
    private val kotlinVersionMinor = KotlinVersion.CURRENT.minor
    private val kotlinVersionPatch = KotlinVersion.CURRENT.patch

    private val expectedEventValue = HashMap<String, Any>().apply {
        put("version", "$kotlinVersionMajor.$kotlinVersionMinor.$kotlinVersionPatch")
        put("major", kotlinVersionMajor)
        put("minor", kotlinVersionMinor)
        put("patch", kotlinVersionPatch)
    }

    @get:Rule
    val timePassedCheckerMockedConstructionRule = MockedConstructionRule(TimePassedChecker::class.java) { mock, _ ->
        whenever(mock.didTimePassMillis(eq(0), eq(TimeUnit.DAYS.toMillis(1)), any()))
            .thenReturn(true)
        whenever(mock.didTimePassMillis(eq(longAgoSendTime), eq(TimeUnit.DAYS.toMillis(1)), any()))
            .thenReturn(true)
        whenever(mock.didTimePassMillis(eq(nearestSendTime), eq(TimeUnit.DAYS.toMillis(1)), any()))
            .thenReturn(false)
    }

    @get:Rule
    val systemTimeProviderMockedConstructionRule = MockedConstructionRule(SystemTimeProvider::class.java) { mock, _ ->
        whenever(mock.currentTimeMillis()).thenReturn(currentTimeMillis)
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val appMetricaSelfReportFacadeMockedStaticRule = MockedStaticRule(AppMetricaSelfReportFacade::class.java)

    private val eventValueCaptor = argumentCaptor<Map<String, Any>>()

    private val reporter = mock<IReporterExtended>()

    private lateinit var preferences: PreferencesServiceDbStorage

    private lateinit var reportKotlinVersionTask: ReportKotlinVersionTask

    @Before
    fun setUp() {
        preferences = GlobalServiceLocator.getInstance().servicePreferences
        whenever(preferences.putLastKotlinVersionSendTime(any())).thenReturn(preferences)
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(reporter)

        reportKotlinVersionTask = ReportKotlinVersionTask()
    }

    @Test
    fun `run for first time`() {
        whenever(preferences.lastKotlinVersionSendTime()).thenReturn(0)
        reportKotlinVersionTask.run()
        verifySendKotlinVersion()
    }

    @Test
    fun `run for last send kotlin version long ago`() {
        whenever(preferences.lastKotlinVersionSendTime()).thenReturn(longAgoSendTime)
        reportKotlinVersionTask.run()
        verifySendKotlinVersion()
    }

    @Test
    fun `run for nearest last known send time`() {
        whenever(preferences.lastKotlinVersionSendTime()).thenReturn(nearestSendTime)
        reportKotlinVersionTask.run()
        verifyNoMoreInteractions(reporter)
        verify(preferences, never()).putLastKotlinVersionSendTime(any())
    }

    @Test
    fun timePassedChecker() {
        reportKotlinVersionTask.run()
        assertThat(extractTimePassedChecker()).isNotNull()
    }

    @Test
    fun timeProvider() {
        reportKotlinVersionTask.run()
        assertThat(extractTimeProvider()).isNotNull()
    }

    private fun verifySendKotlinVersion() {
        verify(reporter).reportEvent(eq("kotlin_version"), eventValueCaptor.capture())
        assertThat(eventValueCaptor.firstValue).containsExactlyInAnyOrderEntriesOf(expectedEventValue)
        inOrder(preferences) {
            verify(preferences).putLastKotlinVersionSendTime(currentTimeMillis)
            verify(preferences).commit()
        }
    }

    private fun extractTimePassedChecker(): TimePassedChecker {
        assertThat(timePassedCheckerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(timePassedCheckerMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return timePassedCheckerMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun extractTimeProvider(): SystemTimeProvider {
        assertThat(systemTimeProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(systemTimeProviderMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return systemTimeProviderMockedConstructionRule.constructionMock.constructed().first()
    }
}
