package io.appmetrica.analytics.impl.telephony

import android.Manifest
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.coreutils.internal.services.telephony.CellularNetworkTypeExtractor
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class MobileConnectionDescriptionExtractorTest : CommonTest() {

    private val networkTypeString = "GPRS"
    private val context = mock<Context>()

    private val cachedMobileConnectionDescription = mock<MobileConnectionDescription>()

    private val mobileConnectionDescriptionCaptor = argumentCaptor<MobileConnectionDescription>()

    @get:Rule
    val cachedDataMockedConstructionRule = MockedConstructionRule(CachedDataProvider.CachedData::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val cellularNetworkTypeExtractorMockedConstruction = constructionRule<CellularNetworkTypeExtractor> {
        on { getNetworkType() } doReturn networkTypeString
    }

    @get:Rule
    val androidUtilsRule = staticRule<AndroidUtils> {
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.P) } doReturn true
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q) } doReturn true
    }
    private val networkTypeExtractor: CellularNetworkTypeExtractor by cellularNetworkTypeExtractorMockedConstruction

    private val cacheExpiryTime = TimeUnit.SECONDS.toMillis(20)

    private val cachedData: CachedDataProvider.CachedData<MobileConnectionDescription> by lazy { cachedData() }
    private lateinit var permissionExtractor: PermissionExtractor

    @Before
    fun setUp() {
        permissionExtractor = GlobalServiceLocator.getInstance().generalPermissionExtractor
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
    }

    @Test
    fun checkNetworkTypeExtractor() {
        MobileConnectionDescriptionExtractor(context)
        assertThat(cellularNetworkTypeExtractorMockedConstruction.argumentInterceptor.flatArguments())
            .containsExactly(context)
    }

    @Test
    fun `extract if cache exists pre Q`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        val extractor = MobileConnectionDescriptionExtractor(context)
        whenever(cachedData.data).thenReturn(cachedMobileConnectionDescription)
        whenever(cachedData.shouldUpdateData()).thenReturn(false)
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        assertThat(extractor.extract()).isEqualTo(cachedMobileConnectionDescription)
        verify(cachedData, never()).data = any()
    }

    @Test
    fun `extract if should update cache pre Q`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        val extractor = MobileConnectionDescriptionExtractor(context)
        whenever(cachedData.data).thenReturn(cachedMobileConnectionDescription)
        whenever(cachedData.shouldUpdateData()).thenReturn(true)
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        val result = extractor.extract()
        verify(cachedData).data = mobileConnectionDescriptionCaptor.capture()
        assertThat(result).isEqualTo(mobileConnectionDescriptionCaptor.firstValue)
        assertThat(mobileConnectionDescriptionCaptor.firstValue).isNotEqualTo(cachedMobileConnectionDescription)
    }

    @Test
    fun `extract on PreQ with read phone state permission`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        val result = MobileConnectionDescriptionExtractor(context).extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    fun `extract on PreQ without read phone state permission`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        val result = MobileConnectionDescriptionExtractor(context).extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    fun `extract on PreQ if networkType is unknown`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        val unknownNetworkType = "unknown"
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE))
            .thenReturn(false)
        val extractor = MobileConnectionDescriptionExtractor(context)
        whenever(networkTypeExtractor.getNetworkType()).thenReturn(unknownNetworkType)
        val result = extractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", unknownNetworkType)
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    fun `extract on PreQ if networkType is null`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        val extractor = MobileConnectionDescriptionExtractor(context)
        whenever(networkTypeExtractor.getNetworkType()).thenReturn(null)
        val result = extractor.extract()
        ObjectPropertyAssertions(result)
            .checkFieldIsNull("networkType")
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    fun `extract on PreQ if networkType is empty`() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(true)
        val extractor = MobileConnectionDescriptionExtractor(context)
        whenever(networkTypeExtractor.getNetworkType()).thenReturn("")
        val result = extractor.extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", "")
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    fun `extract with read phone state permission`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE))
            .thenReturn(true)
        val result = MobileConnectionDescriptionExtractor(context).extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", networkTypeString)
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    fun `extract without read phone state permission`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        val result = MobileConnectionDescriptionExtractor(context).extract()
        ObjectPropertyAssertions(result)
            .checkField("networkType", "unknown")
            .checkAll()
        verify(cachedData).data = result
    }

    private fun cachedData(): CachedDataProvider.CachedData<MobileConnectionDescription> {
        assertThat(cachedDataMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(cachedDataMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(cacheExpiryTime, cacheExpiryTime * 2, "mobile-connection")
        return cachedDataMockedConstructionRule.constructionMock.constructed().first()
            as CachedDataProvider.CachedData<MobileConnectionDescription>
    }
}
