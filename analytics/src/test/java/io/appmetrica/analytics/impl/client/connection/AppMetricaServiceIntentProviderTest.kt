package io.appmetrica.analytics.impl.client.connection

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.AppMetricaConnectionConstants
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.internal.AppMetricaService
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaServiceIntentProviderTest : CommonTest() {

    private val packageName = "test.package.name"
    private val context: Context = mock {
        on { packageName } doReturn packageName
    }
    private val serviceScheme = "test.scheme"
    private val serviceClass: Class<*> = AppMetricaService::class.java
    private val serviceDescription: ServiceDescription = mock {
        on { packageName } doReturn packageName
        on { serviceScheme } doReturn serviceScheme
        on { serviceClass } doReturn serviceClass
    }
    private val screenInfo: ScreenInfo = mock()
    private val screenInfoStringValue = "screen info string value"

    @get:Rule
    val jsonHelperRule = staticRule<JsonHelper> {
        on { JsonHelper.screenInfoToJsonString(screenInfo) } doReturn screenInfoStringValue
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    val metaDataKey = "meta data key"
    val metaDataValue = "meta data value"
    val applicationMetaData = Bundle().apply { putString(metaDataKey, metaDataValue) }

    val applicationInfo: ApplicationInfo = ApplicationInfo().apply {
        metaData = applicationMetaData
    }

    @get:Rule
    val safePackageManagerRule = constructionRule<SafePackageManager> {
        on { it.getApplicationInfo(any(), any(), any()) } doReturn applicationInfo
    }
    private val safePackageManager: SafePackageManager by safePackageManagerRule

    val intentProvider by setUp { AppMetricaServiceIntentProvider() }

    @Before
    fun setUp() {
        whenever(clientServiceLocatorRule.serviceDescriptionProvider.serviceDescription(context))
            .thenReturn(serviceDescription)
        whenever(clientServiceLocatorRule.screenInfoRetriever.retrieveScreenInfo(context))
            .thenReturn(screenInfo)
    }

    @Test
    fun getIntent() {
        val intent = intentProvider.getIntent(context)
        assertThat(intent.action).isEqualTo(AppMetricaConnectionConstants.ACTION_CLIENT_CONNECTION)
        assertThat(intent.data?.scheme).isEqualTo(serviceScheme)
        assertThat(intent.data?.authority).isEqualTo(packageName)
        assertThat(intent.data?.path).isEqualTo("/client")
        assertThat(intent.data?.getQueryParameter("pid")).isEqualTo(android.os.Process.myPid().toString())
        assertThat(intent.data?.getQueryParameter("psid")).isEqualTo(ProcessConfiguration.PROCESS_SESSION_ID)
        assertThat(intent.getStringExtra("screen_size")).isEqualTo(screenInfoStringValue)
        assertThat(intent.extras?.getString(metaDataKey)).isEqualTo(metaDataValue)
    }

    @Test
    fun `getIntent if application info is null`() {
        whenever(safePackageManager.getApplicationInfo(any(), any(), any()))
            .thenReturn(null)
        assertThat(intentProvider.getIntent(context).extras?.keySet()).containsExactly("screen_size")
    }

    @Test
    fun `getIntent if screen info is null`() {
        whenever(clientServiceLocatorRule.screenInfoRetriever.retrieveScreenInfo(context))
            .thenReturn(null)
        assertThat(intentProvider.getIntent(context).getStringExtra("screen_size")).isNull()
    }
}
