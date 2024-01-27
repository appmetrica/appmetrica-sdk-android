package io.appmetrica.analytics.internal

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import io.appmetrica.analytics.impl.AppMetricaServiceCoreExecutionDispatcher
import io.appmetrica.analytics.impl.AppMetricaServiceCoreImpl
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AppMetricaServiceTest : CommonTest() {

    @get:Rule
    val rule = GlobalServiceLocatorRule()

    @get:Rule
    val metricaCoreMockedConstructionRule = constructionRule<AppMetricaServiceCoreImpl>()

    @get:Rule
    val metricaCoreExecutionDispatcherMockedConstructionRule =
        constructionRule<AppMetricaServiceCoreExecutionDispatcher>()

    private val configuration: Configuration = mock()

    private var context: Context = mock()

    private lateinit var metricaService: AppMetricaService

    @Before
    fun setUp() {
        metricaService = spy(AppMetricaService())
        doReturn(RuntimeEnvironment.getApplication()).whenever(metricaService).applicationContext
        doReturn("blabla").whenever(metricaService).packageName
        whenever(metricaService.applicationContext).thenReturn(context)
    }

    @After
    fun tearDown() {
        AppMetricaService.clearInstance()
    }

    @Test
    fun onCreate() {
        metricaService.onCreate()
        assertThat(metricaCoreMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(metricaCoreExecutionDispatcherMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        val mockedCore = metricaCoreExecutionDispatcherMockedConstructionRule.constructionMock.constructed()[0]
        verify(mockedCore).onCreate()
        verify(mockedCore, never()).updateCallback(any())
        clearInvocations(mockedCore)
        metricaService.onCreate()
        assertThat(metricaCoreMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(metricaCoreExecutionDispatcherMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        verify(mockedCore).updateCallback(any())
        verify(mockedCore).onCreate()
    }

    @Test
    fun onBindForWakeLockAction() {
        val intent = Intent()
        intent.action = AppMetricaServiceAction.ACTION_SERVICE_WAKELOCK + "_some_postfix"
        val service = metricaService
        service.onCreate()
        assertThat(service.onBind(intent)).isInstanceOf(AppMetricaService.WakeLockBinder::class.java)
    }

    @Test
    fun onBindForIntentWithoutAction() {
        val service = metricaService
        service.onCreate()
        assertThat(service.onBind(Intent())).isInstanceOf(IAppMetricaService.Stub::class.java)
    }

    @Test
    fun onBindForAnotherAction() {
        val service = metricaService
        service.onCreate()
        val intent = Intent("Some action")
        assertThat(service.onBind(intent)).isInstanceOf(IAppMetricaService.Stub::class.java)
    }

    @Test
    fun onUnbindForWakeLockAction() {
        val intent = Intent()
        intent.action = AppMetricaServiceAction.ACTION_SERVICE_WAKELOCK + "_some_postfix"
        val service = metricaService
        service.onCreate()
        assertThat(service.onUnbind(intent)).isFalse()
    }

    @Test
    fun onUnbindForIntentWithoutAction() {
        val service = metricaService
        service.onCreate()
        assertThat(service.onUnbind(Intent())).isFalse()
    }

    @Test
    fun onUnbindForIntentWithData() {
        val service = metricaService
        service.onCreate()
        val intent = Intent()
        intent.data = Uri.parse("https://yandex.ru")
        assertThat(service.onUnbind(intent)).isTrue()
    }

    @Test
    fun onConfigurationChanged() {
        val service = metricaService
        service.onCreate()
        service.onConfigurationChanged(configuration)
        verify(metricaCoreExecutionDispatcherMockedConstructionRule.constructionMock.constructed()[0])
            .onConfigurationChanged(configuration)
    }
}
