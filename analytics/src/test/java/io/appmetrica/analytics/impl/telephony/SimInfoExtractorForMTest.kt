package io.appmetrica.analytics.impl.telephony

import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.annotation.Config

class SimInfoExtractorForMTest : CommonTest() {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var firstSubscriptionsInfo: SubscriptionInfo

    @Mock
    private lateinit var secondSubscriptionInfo: SubscriptionInfo

    private val firstDataRoaming = SubscriptionManager.DATA_ROAMING_ENABLE
    private val firstCarrierName = "First carrier name"
    private val secondDataRoaming = SubscriptionManager.DATA_ROAMING_DISABLE
    private val secondCarrierName = null

    @Rule
    @JvmField
    val sSystemServiceUtils = MockedStaticRule(SystemServiceUtils::class.java)
    @Rule
    @JvmField
    val sAndroidUtilsMockedRule = MockedStaticRule(AndroidUtils::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(firstSubscriptionsInfo.dataRoaming).thenReturn(firstDataRoaming)
        `when`(firstSubscriptionsInfo.carrierName).thenReturn(firstCarrierName)
        `when`(secondSubscriptionInfo.dataRoaming).thenReturn(secondDataRoaming)
        `when`(secondSubscriptionInfo.carrierName).thenReturn(secondCarrierName)
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun extractSimInfosFromSubscriptionManagerForNullOnP() {
        stubSubscriptionInfos(null)
        assertThat(SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context))
            .isEqualTo(emptyList<SimInfo>())
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun extractSimInfosFromSubscriptionManagerForNullOnQ() {
        `when`(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(true)
        stubSubscriptionInfos(null)
        assertThat(SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context))
            .isEqualTo(emptyList<SimInfo>())
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun extractSimInfosFromSubscriptionManagerForEmptyOnP() {
        stubSubscriptionInfos(emptyList())
        assertThat(SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context))
            .isEqualTo(emptyList<SimInfo>())
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun extractSimInfosFromSubscriptionManagerForEmptyOnQ() {
        `when`(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(true)
        stubSubscriptionInfos(emptyList())
        assertThat(SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context))
            .isEqualTo(emptyList<SimInfo>())
    }

    @Config(sdk = [Build.VERSION_CODES.P])
    @Test
    fun extractSimInfosFromSubscriptionManagerOnP() {
        val firstDeprecatedMcc = 34223
        val firstDeprecatedMnc = 5432
        `when`(firstSubscriptionsInfo.mcc).thenReturn(firstDeprecatedMcc)
        `when`(firstSubscriptionsInfo.mnc).thenReturn(firstDeprecatedMnc)

        stubSubscriptionInfos(listOf(firstSubscriptionsInfo, secondSubscriptionInfo))

        val simInfos = SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context)

        assertThat(simInfos).hasSize(2)

        ObjectPropertyAssertions(simInfos[0])
            .checkField("simCountryCode", firstDeprecatedMcc)
            .checkField("simNetworkCode", firstDeprecatedMnc)
            .checkField("isNetworkRoaming", true)
            .checkField("operatorName", firstCarrierName)
            .checkAll()

        ObjectPropertyAssertions(simInfos[1])
            .checkField("simCountryCode", 0)
            .checkField("simNetworkCode", 0)
            .checkField("isNetworkRoaming", false)
            .checkFieldsAreNull("operatorName")
            .checkAll()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun extractSimInfosFromSubscriptionManagerOnQ() {
        `when`(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(true)
        val firstDeprecatedMcc = 34223
        val firstDeprecatedMnc = 5432
        val secondMcc = 24353
        val secondMnc = 567654
        val secondMccString = secondMcc.toString()
        val secondMncString = secondMnc.toString()
        `when`(firstSubscriptionsInfo.mcc).thenReturn(firstDeprecatedMcc)
        `when`(firstSubscriptionsInfo.mnc).thenReturn(firstDeprecatedMnc)
        `when`(secondSubscriptionInfo.mccString).thenReturn(secondMccString)
        `when`(secondSubscriptionInfo.mncString).thenReturn(secondMncString)

        stubSubscriptionInfos(listOf(firstSubscriptionsInfo, secondSubscriptionInfo))

        val simInfos = SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context)

        assertThat(simInfos).hasSize(2)

        ObjectPropertyAssertions(simInfos[0])
            .checkField("isNetworkRoaming", true)
            .checkField("operatorName", firstCarrierName)
            .checkFieldsAreNull("simCountryCode", "simNetworkCode")
            .checkAll()

        ObjectPropertyAssertions(simInfos[1])
            .checkField("simCountryCode", secondMcc)
            .checkField("simNetworkCode", secondMnc)
            .checkField("isNetworkRoaming", false)
            .checkFieldsAreNull("operatorName")
            .checkAll()
    }

    private fun stubSubscriptionInfos(value: List<SubscriptionInfo>?) {
        `when`(
            SystemServiceUtils.accessSystemServiceByNameSafely<SubscriptionManager, List<SubscriptionInfo>>(
                eq(context),
                eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE),
                any(),
                any(),
                any()
            )
        ).thenReturn(value)
    }
}
