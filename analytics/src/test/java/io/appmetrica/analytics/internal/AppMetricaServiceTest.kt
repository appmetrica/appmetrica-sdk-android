package io.appmetrica.analytics.internal

import android.content.Intent
import android.content.res.Configuration
import io.appmetrica.analytics.impl.service.AppMetricaCoreServiceCallback
import io.appmetrica.analytics.impl.service.AppMetricaServiceProxy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaServiceTest : CommonTest() {

    @get:Rule
    val serviceDelegateRule = constructionRule<AppMetricaServiceProxy>()
    private val serviceDelegate by serviceDelegateRule

    @get:Rule
    val serviceCallbackRule = constructionRule<AppMetricaCoreServiceCallback>()
    private val serviceCallback by serviceCallbackRule

    private val service by setUp { Robolectric.buildService(AppMetricaService::class.java).get() }

    @Test
    fun serviceCallback() {
        service.onCreate()
        assertThat(serviceCallbackRule.constructionMock.constructed()).hasSize(1)
        assertThat(serviceCallbackRule.argumentInterceptor.flatArguments()).containsExactly(service)
    }

    @Test
    fun `before onCreate`() {
        assertThat(serviceDelegateRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun onCreate() {
        service.onCreate()
        verify(serviceDelegate).onCreate()
        clearInvocations(serviceDelegate)
        service.onCreate()
        assertThat(serviceDelegateRule.constructionMock.constructed()).hasSize(1)
        assertThat(serviceDelegateRule.argumentInterceptor.flatArguments()).containsExactly(service, serviceCallback)
        verify(serviceDelegate).onCreate()
    }

    @Test
    fun onStartCommand() {
        service.onCreate()
        service.onStartCommand(Intent(), 0, 0)
        verify(serviceDelegate).onStartCommand(any(), any(), any())
    }

    @Test
    fun onBind() {
        service.onCreate()
        service.onBind(Intent())
        verify(serviceDelegate).onBind(any())
    }

    @Test
    fun onRebind() {
        service.onCreate()
        service.onRebind(Intent())
        verify(serviceDelegate).onRebind(any())
    }

    @Test
    fun onDestroy() {
        service.onCreate()
        service.onDestroy()
        verify(serviceDelegate).onDestroy()
    }

    @Test
    fun onConfigurationChanged() {
        service.onCreate()
        service.onConfigurationChanged(Configuration())
        verify(serviceDelegate).onConfigurationChanged(any())
    }

    @Test
    fun onUnbind() {
        service.onCreate()
        service.onUnbind(Intent())
        verify(serviceDelegate).onUnbind(any())
    }
}
