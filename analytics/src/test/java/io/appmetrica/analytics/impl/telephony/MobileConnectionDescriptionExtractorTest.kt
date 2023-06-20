package io.appmetrica.analytics.impl.telephony

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class MobileConnectionDescriptionExtractorTest : CommonTest() {

    private val networkTypeValue = TelephonyManager.NETWORK_TYPE_GPRS
    private val networkTypeString = "GPRS"
    private val telephonyManager = mock<TelephonyManager> {
        on { networkType } doReturn networkTypeValue
    }
    private val context = mock<Context> {
        on { getSystemService(Context.TELEPHONY_SERVICE) } doReturn telephonyManager
    }

    private val cachedMobileConnectionDescription = mock<MobileConnectionDescription>()

    private val mobileConnectionDescriptionCaptor = argumentCaptor<MobileConnectionDescription>()

    @get:Rule
    val cachedDataMockedConstructionRule = MockedConstructionRule(CachedDataProvider.CachedData::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val cacheExpiryTime = TimeUnit.SECONDS.toMillis(20)

    private lateinit var mobileConnectionDescriptionExtractor: MobileConnectionDescriptionExtractor
    private lateinit var cachedData: CachedDataProvider.CachedData<MobileConnectionDescription>
    private lateinit var permissionExtractor: PermissionExtractor

    @Before
    fun setUp() {
        permissionExtractor = GlobalServiceLocator.getInstance().generalPermissionExtractor
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        mobileConnectionDescriptionExtractor = MobileConnectionDescriptionExtractor(context)
        cachedData = cachedData()
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract if cache exists`() {
        whenever(cachedData.data).thenReturn(cachedMobileConnectionDescription)
        whenever(cachedData.shouldUpdateData()).thenReturn(false)
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        assertThat(mobileConnectionDescriptionExtractor.extract()).isEqualTo(cachedMobileConnectionDescription)
        verify(cachedData, never()).data = any()
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract if should update cache`() {
        whenever(cachedData.data).thenReturn(cachedMobileConnectionDescription)
        whenever(cachedData.shouldUpdateData()).thenReturn(true)
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        val result = mobileConnectionDescriptionExtractor.extract()
        verify(cachedData).data = mobileConnectionDescriptionCaptor.capture()
        assertThat(result).isEqualTo(mobileConnectionDescriptionCaptor.firstValue)
        assertThat(mobileConnectionDescriptionCaptor.firstValue).isNotEqualTo(cachedMobileConnectionDescription)
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract on PreQ with read phone state permission`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract on PreQ without read phone state permission`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract on PreQ if networkType is invalid`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        whenever(telephonyManager.networkType).thenReturn(-999999999)
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", "unknown")
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract on PreQ if networkType throws exception`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        whenever(telephonyManager.networkType).thenThrow(RuntimeException())
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", "unknown")
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract on PreQ if networkOperator is null `() {
        whenever(telephonyManager.networkOperatorName).thenReturn(null)
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun `extract on PreQ if networkOperator is empty `() {
        whenever(telephonyManager.networkOperatorName).thenReturn("")
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun `extract on Q with read phone state permission`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE))
            .thenReturn(true)
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun `extract on Q without read phone state permission`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        val result = mobileConnectionDescriptionExtractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", "unknown")
            .checkAll()
        verify(cachedData).data = result
    }

    private fun cachedData() : CachedDataProvider.CachedData<MobileConnectionDescription> {
        assertThat(cachedDataMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(cachedDataMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(cacheExpiryTime, cacheExpiryTime * 2, "mobile-connection")
        return cachedDataMockedConstructionRule.constructionMock.constructed().first()
            as CachedDataProvider.CachedData<MobileConnectionDescription>
    }
}
