package io.appmetrica.analytics.impl.service

import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import io.appmetrica.analytics.impl.AppMetricaServiceCoreImpl
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.SelfProcessReporter
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppMetricaServiceProxyTest : CommonTest() {

    private val context: Context = mock()
    private val serviceCallback: AppMetricaServiceCallback = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val publicLoggerStaticRule = staticRule<PublicLogger>()

    @get:Rule
    val appmetricaServiceCoreImplRule = constructionRule<AppMetricaServiceCoreImpl>()
    private val appMetricaServiceCore by appmetricaServiceCoreImplRule

    @get:Rule
    val appMetricaServiceDataReporterRule = constructionRule<AppMetricaServiceDataReporter>()

    @get:Rule
    val appMetricaServiceBinderRule = constructionRule<AppMetricaServiceBinder>()

    @get:Rule
    val selfProcessReporterRule = constructionRule<SelfProcessReporter>()

    private val intent: Intent = mock {
        on { data } doReturn mock<Uri>()
    }

    private val serviceProxy by setUp { AppMetricaServiceProxy(context, serviceCallback) }

    @Test
    fun `onCreate init public logger`() {
        serviceProxy.onCreate()
        serviceProxy.onCreate()
        publicLoggerStaticRule.staticMock.verify({ PublicLogger.init(context) }, times(2))
    }

    @Test
    fun `onCreate init core`() {
        serviceProxy.onCreate()
        serviceProxy.onCreate()
        assertThat(appmetricaServiceCoreImplRule.constructionMock.constructed()).hasSize(1)
        assertThat(appmetricaServiceCoreImplRule.argumentInterceptor.flatArguments())
            .containsExactly(context, serviceCallback)
        verify(appMetricaServiceCore, times(2)).onCreate()
    }

    @Test
    fun `onCreate register data reporter`() {
        serviceProxy.onCreate()
        serviceProxy.onCreate()
        verify(GlobalServiceLocator.getInstance().serviceDataReporterHolder)
            .registerServiceDataReporter(
                AppMetricaServiceDataReporter.TYPE_CORE,
                appMetricaServiceDataReporterRule.constructionMock.constructed().first()
            )
        assertThat(appMetricaServiceDataReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(appMetricaServiceDataReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(appmetricaServiceCoreImplRule.constructionMock.constructed().first())
    }

    @Test
    fun `onCreate init core binder`() {
        serviceProxy.onCreate()
        serviceProxy.onCreate()
        assertThat(appMetricaServiceBinderRule.constructionMock.constructed()).hasSize(1)
        assertThat(appMetricaServiceBinderRule.argumentInterceptor.flatArguments())
            .containsExactly(appmetricaServiceCoreImplRule.constructionMock.constructed().first())
    }

    @Test
    fun `onCreate init self process reporter`() {
        serviceProxy.onCreate()
        assertThat(selfProcessReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(selfProcessReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(appmetricaServiceCoreImplRule.constructionMock.constructed().first())

        verify(GlobalServiceLocator.getInstance())
            .initSelfDiagnosticReporterStorage(selfProcessReporterRule.constructionMock.constructed().first())
    }

    @Test
    fun onStart() {
        serviceProxy.onCreate()
        serviceProxy.onStart(intent, 0)
        verify(appMetricaServiceCore).onStart(intent, 0)
    }

    @Test
    fun onStartCommand() {
        serviceProxy.onCreate()
        assertThat(serviceProxy.onStartCommand(intent, 0, 0)).isEqualTo(START_NOT_STICKY)
        verify(appMetricaServiceCore).onStartCommand(intent, 0, 0)
    }

    @Test
    fun onBind() {
        serviceProxy.onCreate()
        assertThat(serviceProxy.onBind(intent))
            .isEqualTo(appMetricaServiceBinderRule.constructionMock.constructed().first())
    }

    @Test
    fun onRebind() {
        serviceProxy.onCreate()
        serviceProxy.onRebind(intent)
        verify(appMetricaServiceCore).onRebind(intent)
    }

    @Test
    fun `onUnbind for wakelock action`() {
        whenever(intent.action).thenReturn(AppMetricaConnectionConstants.ACTION_SERVICE_WAKELOCK)
        serviceProxy.onCreate()
        assertThat(serviceProxy.onUnbind(intent)).isFalse()
        verify(appMetricaServiceCore).onUnbind(intent)
    }

    @Test
    fun `onUnbind for client connect action`() {
        whenever(intent.action).thenReturn(AppMetricaConnectionConstants.ACTION_CLIENT_CONNECTION)
        serviceProxy.onCreate()
        assertThat(serviceProxy.onUnbind(intent)).isFalse()
        verify(appMetricaServiceCore).onUnbind(intent)
    }

    @Test
    fun `onUnbind for other action`() {
        whenever(intent.action).thenReturn("other")
        serviceProxy.onCreate()
        assertThat(serviceProxy.onUnbind(intent)).isFalse()
        verify(appMetricaServiceCore).onUnbind(intent)
    }

    @Test
    fun `onUnbind for null data intent`() {
        whenever(intent.data).thenReturn(null)
        serviceProxy.onCreate()
        assertThat(serviceProxy.onUnbind(intent)).isTrue()
        verify(appMetricaServiceCore).onUnbind(intent)
    }

    @Test
    fun onDestroy() {
        serviceProxy.onCreate()
        serviceProxy.onDestroy()
        verify(appMetricaServiceCore).onDestroy()
    }

    @Test
    fun onConfigurationChanged() {
        val configuration: Configuration = mock()
        serviceProxy.onCreate()
        serviceProxy.onConfigurationChanged(configuration)
        verify(appMetricaServiceCore).onConfigurationChanged(configuration)
    }
}
