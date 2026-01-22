package io.appmetrica.analytics.impl.utils

import android.content.Context
import android.content.pm.ServiceInfo
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.internal.AppMetricaService
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaServiceProcessDetectorTest : CommonTest() {

    private val mainProcess = "test.sample.com"
    private val appMetricaProcess = "test.sample.com:AppMetrica"

    private val context: Context = mock()
    private val serviceInfo = ServiceInfo().apply {
        processName = appMetricaProcess
    }

    @get:Rule
    val packageManagerUtilsMockedStaticRule = staticRule<PackageManagerUtils> {
        on { PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java) } doReturn serviceInfo
    }

    private val detector: AppMetricaServiceProcessDetector by setUp {
        AppMetricaServiceProcessDetector()
    }

    @Test
    fun `isMainProcess for AppMetrica process`() {
        assertThat(detector.isMainProcess(context)).isFalse()
    }

    @Test
    fun `isNonMainProcess for AppMetrica process`() {
        assertThat(detector.isNonMainProcess(context)).isTrue()
    }

    @Test
    fun `processName for AppMetrica process`() {
        assertThat(detector.processName(context)).isSameAs(appMetricaProcess)
    }

    @Test
    fun `isMainProcess for main process`() {
        whenever(PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java))
            .thenReturn(ServiceInfo().apply { processName = mainProcess })
        assertThat(detector.isMainProcess(context)).isTrue()
    }

    @Test
    fun `isNonMainProcess for main process`() {
        whenever(PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java))
            .thenReturn(ServiceInfo().apply { processName = mainProcess })
        assertThat(detector.isNonMainProcess(context)).isFalse()
    }

    @Test
    fun `processName for main process`() {
        whenever(PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java))
            .thenReturn(ServiceInfo().apply { processName = mainProcess })
        assertThat(detector.processName(context)).isSameAs(mainProcess)
    }

    @Test
    fun `isMainProcess for null service info`() {
        whenever(PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java)).thenReturn(null)
        assertThat(detector.isMainProcess(context)).isTrue()
    }

    @Test
    fun `isNonMainProcess for null service info`() {
        whenever(PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java)).thenReturn(null)
        assertThat(detector.isNonMainProcess(context)).isFalse()
    }

    @Test
    fun `processName for null service info`() {
        whenever(PackageManagerUtils.getServiceInfo(context, AppMetricaService::class.java)).thenReturn(null)
        assertThat(detector.processName(context)).isNull()
    }
}
