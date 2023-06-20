package io.appmetrica.analytics.impl.telephony

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateHolder
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Collections
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
internal class SimInfoExtractorTest : CommonTest() {

    private val firstSimInfo = mock<SimInfo>()
    private val secondSimInfo = mock<SimInfo>()
    private val simInfos = listOf(firstSimInfo, secondSimInfo)

    private val mnc = 123
    private val mcc = 456
    private val simOperatorValue = mcc.toString() + mnc.toString()
    private val simOperatorNameValue = "Sim operator name"
    private val networkRoamingValue = true
    private val telephonyManager = mock<TelephonyManager> {
        on { simOperator } doReturn simOperatorValue
        on { simOperatorName } doReturn simOperatorNameValue
        on { isNetworkRoaming } doReturn networkRoamingValue
    }
    private val context = mock<Context> {
        on { getSystemService(Context.TELEPHONY_SERVICE) } doReturn telephonyManager
    }

    @get:Rule
    val cachedDataMockedConstructionRule = MockedConstructionRule(CachedDataProvider.CachedData::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val simInfoExtractorForMMockedStaticRule = MockedStaticRule(SimInfoExtractorForM::class.java)

    private lateinit var simInfoExtractor: SimInfoExtractor
    private lateinit var cachedData: CachedDataProvider.CachedData<List<SimInfo>>
    private lateinit var startupStateHolder: StartupStateHolder
    private lateinit var permissionExtractor: PermissionExtractor

    @Before
    fun setUp() {
        permissionExtractor = GlobalServiceLocator.getInstance().generalPermissionExtractor
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(true)
        simInfoExtractor = SimInfoExtractor(context)
        cachedData = cachedData()
        startupStateHolder = GlobalServiceLocator.getInstance().startupStateHolder
        setUpSimInfoCollectingFlags(true)
        whenever(SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context))
            .thenReturn(simInfos)
    }

    @Test
    fun `extract if cache is actual`() {
        whenever(cachedData.shouldUpdateData()).thenReturn(false)
        whenever(cachedData.data).thenReturn(simInfos)
        assertThat(simInfoExtractor.extract()).isEqualTo(simInfos)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `extract if cache non empty but expired`() {
        whenever(cachedData.shouldUpdateData()).thenReturn(true)
        whenever(cachedData.data).thenReturn(emptyList())
        assertThat(simInfoExtractor.extract()).containsExactlyElementsOf(simInfos)
        verify(cachedData).data = simInfos
    }

    @Test
    fun `extract if couldNotCollectSimInfo`() {
        setUpSimInfoCollectingFlags(false)
        assertThat(simInfoExtractor.extract()).isEmpty()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M`() {
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if sim operator is null`() {
        whenever(telephonyManager.simOperator).thenReturn(null)
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode")
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on PreM if sim operator is empty`() {
        whenever(telephonyManager.simOperator).thenReturn("")
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode")
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on PreM if sim operator is invalid`() {
        whenever(telephonyManager.simOperator).thenReturn("Invalid string")
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode")
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if sim operator throws exception`() {
        whenever(telephonyManager.simOperator).thenThrow(RuntimeException())
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode")
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if sim operator name is null`() {
        whenever(telephonyManager.simOperatorName).thenReturn(null)
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkFieldIsNull("operatorName")
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if sim operator name is empty`() {
        whenever(telephonyManager.simOperatorName).thenReturn("")
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", "")
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if sim operator name throws exception`() {
        whenever(telephonyManager.simOperatorName).thenThrow(RuntimeException())
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkFieldIsNull("operatorName")
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if not read phone state`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", false)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if isNetworkRoaming is false`() {
        whenever(telephonyManager.isNetworkRoaming).thenReturn(false)
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", false)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if isNetworkRoaming throws exception`() {
        whenever(telephonyManager.isNetworkRoaming).thenThrow(RuntimeException())
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", false)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if telephony manager is null`() {
        whenever(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null)
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode", "operatorName")
            .checkField("isNetworkRoaming", false)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `extract on pre M if telephony manager throws exception`() {
        whenever(context.getSystemService(Context.TELEPHONY_SERVICE)).thenThrow(RuntimeException())
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode", "operatorName")
            .checkField("isNetworkRoaming", false)
            .checkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `extract on M`() {
        val result = simInfoExtractor.extract()
        assertThat(result).containsExactlyElementsOf(simInfos)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `extract on M if no read phone state`() {
        whenever(permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)).thenReturn(false)
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", false)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
        verify(cachedData).data = result
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `extract on M if SimInfoExtractorFromM returns empty list`() {
        whenever(SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context))
            .thenReturn(Collections.emptyList())
        val result = simInfoExtractor.extract()
        assertThat(result)
            .isNotNull
            .hasSize(1)
        val simInfo = result?.first()
        ObjectPropertyAssertions(simInfo)
            .checkField("simCountryCode", mcc)
            .checkField("simNetworkCode", mnc)
            .checkField("isNetworkRoaming", networkRoamingValue)
            .checkField("operatorName", simOperatorNameValue)
            .checkAll()
        verify(cachedData).data = result
    }

    private fun setUpSimInfoCollectingFlags(value: Boolean) {
        val collectingFlags = CollectingFlags.CollectingFlagsBuilder().withSimInfo(value).build()
        val startupState = startupStateHolder.getStartupState().buildUpon(collectingFlags).build()
        whenever(startupStateHolder.getStartupState()).thenReturn(startupState)
    }

    private fun cachedData(): CachedDataProvider.CachedData<List<SimInfo>> {
        val cacheExpiryTime = TimeUnit.SECONDS.toMillis(20)
        assertThat(cachedDataMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(cachedDataMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(cacheExpiryTime, cacheExpiryTime, "sim-info")
        return cachedDataMockedConstructionRule.constructionMock.constructed().first()
            as CachedDataProvider.CachedData<List<SimInfo>>
    }
}
